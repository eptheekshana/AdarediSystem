package com.bedsheet.orders.repository;

import com.bedsheet.orders.model.CustomerOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    boolean existsByWaybillNo(String waybillNo);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findWithItemsByWaybillNo(String waybillNo);
}
