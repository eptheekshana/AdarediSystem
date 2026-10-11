package com.bedsheet.orders.repository;

import com.bedsheet.orders.model.BedSize;
import com.bedsheet.orders.model.BedsheetType;
import com.bedsheet.orders.model.PricingRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findByItemKey(String itemKey);
    Optional<PricingRule> findByBedsheetTypeAndBedSize(BedsheetType bedsheetType, BedSize bedSize);
    List<PricingRule> findAllByOrderByIdAsc();
}
