package com.frytes.cinemaPlus.booking.service;

import com.frytes.cinemaPlus.booking.entity.PricingRule;
import com.frytes.cinemaPlus.booking.service.pricing.PriceCalculator;
import com.frytes.cinemaPlus.booking.service.pricing.PricingRulesService;
import com.frytes.cinemaPlus.booking.service.pricing.PricingStrategy;
import com.frytes.cinemaPlus.booking.service.pricing.VipStrategy;
import com.frytes.cinemaPlus.content.entity.Seat;
import com.frytes.cinemaPlus.content.entity.Session;
import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("🧮 Тест правильного  расчета цены билета")
class PriceCalculatorTest {

    @Mock private PricingRulesService pricingRulesService;
    private PriceCalculator priceCalculator;

    @BeforeEach
    void setUp() {
        List<PricingStrategy> strategies = List.of(new VipStrategy());
        priceCalculator = new PriceCalculator(pricingRulesService, strategies);
    }

    @Test
    @DisplayName("✅ Должен правильно считать VIP наценку")
    void calculateTotal_WithVip() {
        Session session = new Session();
        session.setBasePrice(BigDecimal.valueOf(300));

        Seat standard = new Seat(); standard.setType(SeatType.STANDARD);
        Seat vip = new Seat(); vip.setType(SeatType.VIP);
        List<Seat> seats = List.of(standard, vip);

        PricingRule vipRule = new PricingRule("VIP_SURCHARGE", BigDecimal.valueOf(200), true);
        when(pricingRulesService.getAllRulesMap()).thenReturn(Map.of("VIP_SURCHARGE", vipRule));

        BigDecimal total = priceCalculator.calculateTotal(session, seats);

        assertEquals(BigDecimal.valueOf(800), total);
    }
}