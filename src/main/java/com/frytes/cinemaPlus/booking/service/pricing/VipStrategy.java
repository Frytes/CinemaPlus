package com.frytes.cinemaPlus.booking.service.pricing;

import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VipStrategy implements PricingStrategy {

    @Override
    public String getRuleName() {
        return "VIP_SURCHARGE";
    }

    @Override
    public BigDecimal calculate(BigDecimal currentPrice, Session session, Seat seat, BigDecimal ruleAmount) {
        if (seat.getType() == SeatType.VIP) {
            return currentPrice.add(ruleAmount);
        }
        return currentPrice;
    }
}