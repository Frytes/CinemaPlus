package com.frytes.cinemaPlus.booking.service.pricing;

import com.frytes.cinemaPlus.booking.entity.PricingRule;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final PricingRulesService pricingRulesService;
    private final List<PricingStrategy> strategies;

    public Map<String, PricingRule> getActiveRules() {
        return pricingRulesService.getAllRulesMap();
    }

    public BigDecimal calculatePrice(Session session, Seat seat, Map<String, PricingRule> rules) {
        BigDecimal price = session.getBasePrice();

        for (PricingStrategy strategy : strategies) {
            PricingRule rule = rules.get(strategy.getRuleName());
            if (rule != null && Boolean.TRUE.equals(rule.getIsActive())) {
                price = strategy.calculate(price, session, seat, rule.getAmount());
            }
        }
        return price;
    }
}
