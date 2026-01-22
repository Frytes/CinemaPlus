package com.frytes.cinemaPlus.booking.service.pricing;

import com.frytes.cinemaPlus.content.entity.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("💰 Unit: Стратегия утренней скидки (MorningDiscountStrategy)")
class MorningDiscountStrategyTest {

    private final MorningDiscountStrategy strategy = new MorningDiscountStrategy();

    @Test
    @DisplayName("✅ Должен применить скидку, если сеанс начинается до 14:00 (утро)")
    void shouldApplyDiscount_WhenSessionIsInTheMorning() {
        // Given
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().withHour(10).withMinute(0));

        BigDecimal currentPrice = BigDecimal.valueOf(300);
        BigDecimal discountAmount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = strategy.calculate(currentPrice, session, null, discountAmount);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    @DisplayName("❌ Не должен применять скидку, если сеанс начинается в 14:00 или позже (вечер)")
    void shouldNotApplyDiscount_WhenSessionIsInTheEvening() {
        // Given
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().withHour(18).withMinute(30));

        BigDecimal currentPrice = BigDecimal.valueOf(300);
        BigDecimal discountAmount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = strategy.calculate(currentPrice, session, null, discountAmount);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    @DisplayName("🛡️ Цена не должна уходить в минус (если скидка больше цены)")
    void shouldReturnZero_WhenDiscountIsGreaterThanPrice() {
        // Given
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().withHour(9).withMinute(0));

        BigDecimal currentPrice = BigDecimal.valueOf(40);
        BigDecimal discountAmount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = strategy.calculate(currentPrice, session, null, discountAmount);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}