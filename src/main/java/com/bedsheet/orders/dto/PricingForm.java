package com.bedsheet.orders.dto;

import java.util.ArrayList;
import java.util.List;

public class PricingForm {

    private List<PricingRuleUpdateDto> rules = new ArrayList<>();

    public PricingForm() {
    }

    public PricingForm(List<PricingRuleUpdateDto> rules) {
        this.rules = rules;
    }

    public List<PricingRuleUpdateDto> getRules() {
        return rules;
    }

    public void setRules(List<PricingRuleUpdateDto> rules) {
        this.rules = rules;
    }
}
