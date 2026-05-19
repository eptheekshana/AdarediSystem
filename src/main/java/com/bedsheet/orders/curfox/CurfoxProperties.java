package com.bedsheet.orders.curfox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "curfox")
public class CurfoxProperties {

    private boolean enabled = true;
    private String apiBaseUrl = "https://v1.api.curfox.com";
    private String listsApiBaseUrl = "https://v2-lists.api.curfox.com/api";
    private String tenant = "royalexpress";
    private String email = "";
    private String password = "";
    private String merchantBusinessId = "";
    private String merchantBusinessName = "";
    private String originCityName = "";
    private String originStateName = "";
    private boolean autoSyncOnCreate = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getListsApiBaseUrl() {
        return listsApiBaseUrl;
    }

    public void setListsApiBaseUrl(String listsApiBaseUrl) {
        this.listsApiBaseUrl = listsApiBaseUrl;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getEmail() {
        return trim(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return trim(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMerchantBusinessId() {
        return merchantBusinessId;
    }

    public void setMerchantBusinessId(String merchantBusinessId) {
        this.merchantBusinessId = merchantBusinessId;
    }

    public String getMerchantBusinessName() {
        return trim(merchantBusinessName);
    }

    public void setMerchantBusinessName(String merchantBusinessName) {
        this.merchantBusinessName = merchantBusinessName;
    }

    public String getOriginCityName() {
        return originCityName;
    }

    public void setOriginCityName(String originCityName) {
        this.originCityName = originCityName;
    }

    public String getOriginStateName() {
        return originStateName;
    }

    public void setOriginStateName(String originStateName) {
        this.originStateName = originStateName;
    }

    public boolean isAutoSyncOnCreate() {
        return autoSyncOnCreate;
    }

    public void setAutoSyncOnCreate(boolean autoSyncOnCreate) {
        this.autoSyncOnCreate = autoSyncOnCreate;
    }

    public boolean isConfigured() {
        return enabled
                && hasText(tenant)
                && hasText(getEmail())
                && hasText(getPassword())
                && hasText(merchantBusinessId)
                && hasText(originCityName)
                && hasText(originStateName);
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
