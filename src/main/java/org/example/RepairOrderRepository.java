package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface RepairOrderRepository
        extends JpaRepository<RepairOrder, Long> {

    @Override
    @EntityGraph(attributePaths = {"jobs", "car", "car.owner"})
    Optional<RepairOrder> findById(Long id);
}