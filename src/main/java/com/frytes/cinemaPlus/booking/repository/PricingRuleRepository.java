package com.frytes.cinemaPlus.booking.repository;

import com.frytes.cinemaPlus.booking.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingRuleRepository extends JpaRepository<PricingRule, String> {
}
