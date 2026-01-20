package com.frytes.cinemaPlus.booking.service.pricing;

import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import java.math.BigDecimal;


public interface PricingStrategy {
    String getRuleName();
    BigDecimal calculate(BigDecimal currentPrice, Session session, Seat seat, BigDecimal ruleAmount);
}