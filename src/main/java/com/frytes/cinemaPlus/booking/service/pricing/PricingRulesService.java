package com.frytes.cinemaPlus.booking.service.pricing;

import com.frytes.cinemaPlus.booking.entity.PricingRule;
import com.frytes.cinemaPlus.booking.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingRulesService {

    private final PricingRuleRepository repository;

    @Cacheable(value = "pricingRules", key = "'all'")
    public Map<String, PricingRule> getAllRulesMap() {
        log.info("Cache miss! Fetching pricing rules from DB...");
        return repository.findAll().stream()
                .collect(Collectors.toMap(PricingRule::getRuleName, rule -> rule));
    }

    @CacheEvict(value = "pricingRules", key = "'all'")
    public void clearCache() {
        log.info("Cache evicted for pricing rules");
    }
}