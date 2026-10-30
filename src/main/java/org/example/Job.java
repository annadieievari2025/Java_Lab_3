package org.example;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder order;

    protected Job() {
    }

    public Job(String description, BigDecimal cost) {
        if (description == null || description.isBlank()) {
            throw new CarServiceException(
                    "Job description can't be null"
            );
        }

        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new CarServiceException(
                    "Cost can't be negative or null"
            );
        }

        this.description = description;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public RepairOrder getOrder() {
        return order;
    }

    void setOrder(RepairOrder order) {
        this.order = order;
    }
}