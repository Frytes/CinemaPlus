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

    public BigDecimal calculateTotal(Session session, List<Seat> seats) {
        Map<String, PricingRule> rules = pricingRulesService.getAllRulesMap();

        BigDecimal total = BigDecimal.ZERO;

        for (Seat seat : seats) {
            BigDecimal ticketPrice = session.getBasePrice();

            for (PricingStrategy strategy : strategies) {
                PricingRule rule = rules.get(strategy.getRuleName());

                if (rule != null && Boolean.TRUE.equals(rule.getIsActive())) {
                    ticketPrice = strategy.calculate(ticketPrice, session, seat, rule.getAmount());
                }
            }

            total = total.add(ticketPrice);
        }
        return total;
    }
}
