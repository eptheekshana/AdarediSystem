package com.bedsheet.orders.curfox;

import com.bedsheet.orders.curfox.dto.CurfoxCity;
import com.bedsheet.orders.curfox.dto.CurfoxTrackingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class CurfoxClient {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    private final CurfoxProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private volatile String cachedToken;
    private final Object tokenLock = new Object();

    public CurfoxClient(CurfoxProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String login() {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("email", properties.getEmail());
        body.put("password", properties.getPassword());

        JsonNode response;
        try {
            response = exchange(
                    HttpMethod.POST,
                    "/api/public/merchant/login",
                    body,
                    null,
                    false);
        } catch (CurfoxApiException ex) {
            if (ex.getMessage().contains("401")) {
                throw new CurfoxApiException(
                        "Royal Express login failed (401). Check CURFOX_EMAIL and CURFOX_PASSWORD in .env — "
                                + "quote the password if it contains # (e.g. CURFOX_PASSWORD=\"your#pass\")");
            }
            throw ex;
        }

        String token = response.path("token").asText(null);
        if (token == null || token.isBlank()) {
            throw new CurfoxApiException("Curfox login failed: no token returned");
        }
        cachedToken = token;
        return token;
    }

    public List<Map<String, Object>> listBusinesses() {
        JsonNode response = authorizedGet("/api/public/merchant/business?noPagination=1");
        return toMapList(response.path("data"));
    }

    public List<CurfoxCity> findCitiesByName(String nameQuery) {
        String encoded = URLEncoder.encode(nameQuery.trim(), StandardCharsets.UTF_8);
        JsonNode response = authorizedGet("/api/public/merchant/city?noPagination=1&filter[name]=" + encoded);
        List<CurfoxCity> cities = new ArrayList<>();
        for (JsonNode node : response.path("data")) {
            cities.add(parseCity(node));
        }
        return cities;
    }

    public Optional<CurfoxCity> findBestCityMatch(String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return Optional.empty();
        }
        String query = nameQuery.trim();
        List<CurfoxCity> cities = findCitiesByName(query);
        if (cities.isEmpty()) {
            return Optional.empty();
        }
        String lower = query.toLowerCase();
        for (CurfoxCity city : cities) {
            if (city.getName().equalsIgnoreCase(query)) {
                return Optional.of(city);
            }
        }
        for (CurfoxCity city : cities) {
            if (city.getName().toLowerCase().contains(lower)) {
                return Optional.of(city);
            }
        }
        return Optional.of(cities.get(0));
    }

    public List<String> createSingleOrder(Map<String, Object> generalData, Map<String, Object> orderLine) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("general_data", objectMapper.valueToTree(generalData));

        ArrayNode orderData = objectMapper.createArrayNode();
        orderData.add(objectMapper.valueToTree(orderLine));
        root.set("order_data", orderData);

        JsonNode response = exchange(
                HttpMethod.POST,
                "/api/public/merchant/order/single",
                root,
                getToken(),
                true);

        List<String> waybills = new ArrayList<>();
        if (response.path("data").isArray()) {
            response.path("data").forEach(node -> waybills.add(node.asText()));
        }
        return waybills;
    }

    public List<CurfoxTrackingEvent> getTracking(String waybillNumber) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("waybill_number", waybillNumber);

        JsonNode response = exchange(
                HttpMethod.GET,
                "/api/public/merchant/order/tracking-info",
                body,
                getToken(),
                true);

        List<CurfoxTrackingEvent> events = new ArrayList<>();
        for (JsonNode node : response.path("data")) {
            CurfoxTrackingEvent event = new CurfoxTrackingEvent();
            event.setStatusName(node.path("status").path("name").asText("—"));
            String dateTime = node.path("date_time").asText(null);
            if (dateTime != null && !dateTime.isBlank()) {
                event.setDateTime(LocalDateTime.parse(dateTime.substring(0, 19)));
            }
            event.setDateTimeAgo(node.path("date_time_ago").asText(""));
            String first = node.path("user").path("first_name").asText("");
            String last = node.path("user").path("last_name").asText("");
            event.setUserName((first + " " + last).trim());
            events.add(event);
        }
        return events;
    }

    /**
     * Fetches Royal Express delivery charge for an order already in Curfox (v2 Lists API).
     */
    public Optional<BigDecimal> fetchDeliveryChargeByWaybill(String waybillNumber) {
        if (waybillNumber == null || waybillNumber.isBlank()) {
            return Optional.empty();
        }
        String encoded = URLEncoder.encode(waybillNumber.trim(), StandardCharsets.UTF_8);
        JsonNode response = listsExchange(
                HttpMethod.GET,
                "/merchant/order?filter[waybill_number]=" + encoded + "&noPagination=1",
                null,
                getToken(),
                true);
        JsonNode data = response.path("data");
        if (!data.isArray() || data.isEmpty()) {
            return Optional.empty();
        }
        JsonNode chargeNode = data.get(0).path("delivery_charge");
        if (chargeNode.isMissingNode() || chargeNode.isNull()) {
            return Optional.empty();
        }
        return Optional.of(BigDecimal.valueOf(chargeNode.asDouble()));
    }

    private JsonNode authorizedGet(String path) {
        return exchange(HttpMethod.GET, path, null, getToken(), true);
    }

    private String getToken() {
        if (cachedToken != null) {
            return cachedToken;
        }
        synchronized (tokenLock) {
            if (cachedToken == null) {
                login();
            }
            return cachedToken;
        }
    }

    private void invalidateToken() {
        cachedToken = null;
    }

    private JsonNode exchange(
            HttpMethod method,
            String path,
            JsonNode body,
            String token,
            boolean retryOnUnauthorized) {
        return exchangeOnBase(properties.getApiBaseUrl(), method, path, body, token, retryOnUnauthorized);
    }

    private JsonNode listsExchange(
            HttpMethod method,
            String path,
            JsonNode body,
            String token,
            boolean retryOnUnauthorized) {
        return exchangeOnBase(properties.getListsApiBaseUrl(), method, path, body, token, retryOnUnauthorized);
    }

    private JsonNode exchangeOnBase(
            String baseUrl,
            HttpMethod method,
            String path,
            JsonNode body,
            String token,
            boolean retryOnUnauthorized) {
        try {
            return doExchange(baseUrl, method, path, body, token);
        } catch (HttpStatusCodeException ex) {
            if (retryOnUnauthorized && ex.getStatusCode().value() == 401) {
                synchronized (tokenLock) {
                    invalidateToken();
                    login();
                    return doExchange(baseUrl, method, path, body, cachedToken);
                }
            }
            throw toApiException(ex);
        } catch (CurfoxApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CurfoxApiException("Curfox API call failed: " + ex.getMessage(), ex);
        }
    }

    private JsonNode doExchange(String baseUrl, HttpMethod method, String path, JsonNode body, String token) {
        HttpHeaders headers = baseHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<String> entity;
        if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(body.toString(), headers);
        } else if (body != null && method == HttpMethod.GET) {
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(body.toString(), headers);
        } else {
            entity = new HttpEntity<>(headers);
        }

        String url = baseUrl + path;
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
        try {
            if (response.getBody() == null || response.getBody().isBlank()) {
                return objectMapper.createObjectNode();
            }
            return objectMapper.readTree(response.getBody());
        } catch (Exception ex) {
            throw new CurfoxApiException("Failed to parse Curfox response", ex);
        }
    }

    private HttpHeaders baseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("X-tenant", properties.getTenant());
        return headers;
    }

    private CurfoxApiException toApiException(HttpStatusCodeException ex) {
        String body = ex.getResponseBodyAsString();
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) {
                String message = node.get("message").asText();
                if (node.has("errors")) {
                    JsonNode errors = node.get("errors");
                    String friendly = formatValidationErrors(errors);
                    if (friendly != null) {
                        return new CurfoxApiException(friendly);
                    }
                    message = message + " — " + errors;
                }
                return new CurfoxApiException(message);
            }
        } catch (Exception ignored) {
            // use raw body
        }
        return new CurfoxApiException("Curfox API error (" + ex.getStatusCode().value() + "): " + body);
    }

    private String formatValidationErrors(JsonNode errors) {
        if (errors.has("rate_card.destination_city_id")) {
            String detail = errors.get("rate_card.destination_city_id").get(0).asText();
            return "Royal Express rate card: " + detail
                    + " Ask Royal Express to add this route to your Adaredi.com rate card, "
                    + "or change CURFOX_ORIGIN_CITY_NAME in .env if you ship from a different location.";
        }
        if (errors.has("order_data.0.destination_city_name")) {
            return "Invalid destination city for Royal Express. Use exact city names from their system "
                    + "(e.g. Kadawatha with state Gampaha).";
        }
        if (errors.has("order_data.0.destination_state_name")) {
            return "Invalid destination state for Royal Express. Use the state name from their city list "
                    + "(e.g. Gampaha for Kadawatha, Colombo for Colombo areas).";
        }
        if (errors.has("order_data.0.waybill_number")) {
            return errors.get("order_data.0.waybill_number").get(0).asText();
        }
        return null;
    }

    private static CurfoxCity parseCity(JsonNode node) {
        String stateName = node.path("state").path("name").asText("");
        return new CurfoxCity(
                node.path("id").asText(),
                node.path("name").asText(),
                stateName);
    }

    private List<Map<String, Object>> toMapList(JsonNode array) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (!array.isArray()) {
            return list;
        }
        array.forEach(node -> {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(e -> {
                if (e.getValue().isValueNode()) {
                    map.put(e.getKey(), e.getValue().asText());
                } else if (e.getValue().isNumber()) {
                    map.put(e.getKey(), e.getValue().numberValue());
                } else if (e.getValue().isBoolean()) {
                    map.put(e.getKey(), e.getValue().booleanValue());
                }
            });
            list.add(map);
        });
        return list;
    }
}
