package com.frytes.cinemaPlus.booking.controller;

import com.frytes.cinemaPlus.booking.dto.UpdateRuleRequest;
import com.frytes.cinemaPlus.booking.entity.PricingRule;
import com.frytes.cinemaPlus.booking.service.pricing.PricingRulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingRulesService pricingRulesService;

    @GetMapping
    public ResponseEntity<List<PricingRule>> getAllRules() {
        return ResponseEntity.ok(pricingRulesService.getAllRules());
    }

    @PutMapping("/{ruleName}")
    public ResponseEntity<Void> updateRule(
            @PathVariable String ruleName,
            @RequestBody UpdateRuleRequest request
    ) {
        pricingRulesService.updateRule(ruleName, request.amount(), request.isActive());
        return ResponseEntity.ok().build();
    }
}


