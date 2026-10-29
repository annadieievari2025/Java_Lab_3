package org.example;

import java.math.BigDecimal;

public class Job {
    private String description;
    private BigDecimal cost;

    public Job(String description, BigDecimal cost) {
        if (description == null || description.isBlank()) {
            throw new CarServiceException("Job description can't be null");
        }
        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new CarServiceException("Cost can't be negative or null");
        }
        this.description = description;
        this.cost = cost;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }
}