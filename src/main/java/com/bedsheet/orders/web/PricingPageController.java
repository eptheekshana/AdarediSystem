package com.bedsheet.orders.web;

import com.bedsheet.orders.dto.PricingForm;
import com.bedsheet.orders.dto.PricingRuleUpdateDto;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.model.PricingRule;
import com.bedsheet.orders.service.PricingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PricingPageController {

    private final PricingService pricingService;

    public PricingPageController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/pricing")
    public String index(Model model) {
        List<PricingRule> rules = pricingService.getAllRules();

        List<PricingRule> flatRules = new ArrayList<>();
        List<PricingRule> fittedRules = new ArrayList<>();
        List<PricingRule> addonRules = new ArrayList<>();

        BigDecimal minProfit = null;
        BigDecimal maxProfit = null;
        BigDecimal totalMargin = BigDecimal.ZERO;
        int countForMargin = 0;

        for (PricingRule r : rules) {
            if (r.getBedsheetType() == BedsheetType.FLAT) {
                flatRules.add(r);
            } else if (r.getBedsheetType() == BedsheetType.FITTED) {
                fittedRules.add(r);
            } else {
                addonRules.add(r);
            }

            BigDecimal profit = r.getUnitProfit();
            if (minProfit == null || profit.compareTo(minProfit) < 0) {
                minProfit = profit;
            }
            if (maxProfit == null || profit.compareTo(maxProfit) > 0) {
                maxProfit = profit;
            }

            BigDecimal margin = r.getMarginPercentage();
            if (margin != null && margin.compareTo(BigDecimal.ZERO) > 0) {
                totalMargin = totalMargin.add(margin);
                countForMargin++;
            }
        }

        BigDecimal avgMargin = countForMargin > 0
                ? totalMargin.divide(BigDecimal.valueOf(countForMargin), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PricingForm form = new PricingForm();
        List<PricingRuleUpdateDto> dtos = new ArrayList<>();
        for (PricingRule r : rules) {
            dtos.add(new PricingRuleUpdateDto(r.getItemKey(), r.getSellingPrice(), r.getUnitProfit()));
        }
        form.setRules(dtos);

        model.addAttribute("rules", rules);
        model.addAttribute("flatRules", flatRules);
        model.addAttribute("fittedRules", fittedRules);
        model.addAttribute("addonRules", addonRules);
        model.addAttribute("pricingForm", form);
        model.addAttribute("totalRulesCount", rules.size());
        model.addAttribute("minProfit", minProfit != null ? minProfit : BigDecimal.ZERO);
        model.addAttribute("maxProfit", maxProfit != null ? maxProfit : BigDecimal.ZERO);
        model.addAttribute("avgMargin", avgMargin);

        return "pricing/index";
    }

    @PostMapping("/pricing")
    public String updatePricing(
            @ModelAttribute("pricingForm") PricingForm form,
            RedirectAttributes redirectAttributes) {
        try {
            pricingService.updateRules(form.getRules());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Profit & pricing rates updated successfully! Only new orders created from now on will use these rates. Previously created orders remain completely unchanged.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to update rates: " + ex.getMessage());
        }
        return "redirect:/pricing";
    }

    @PostMapping("/pricing/reset")
    public String resetPricing(RedirectAttributes redirectAttributes) {
        try {
            pricingService.resetToDefaults();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Profit & pricing rates have been reset to factory defaults. Previously created orders remain completely unchanged.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to reset rates: " + ex.getMessage());
        }
        return "redirect:/pricing";
    }
}
