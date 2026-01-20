package com.frytes.cinemaPlus.booking.service.pricing;

import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MorningDiscountStrategy implements PricingStrategy {

    @Override
    public String getRuleName() {
        return "MORNING_DISCOUNT";
    }

    @Override
    public BigDecimal calculate(BigDecimal currentPrice, Session session, Seat seat, BigDecimal ruleAmount) {
        if (session.getStartTime().getHour() < 14) {
            return currentPrice.subtract(ruleAmount).max(BigDecimal.ZERO);
        }
        return currentPrice;
    }
}
