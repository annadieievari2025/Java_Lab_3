package org.example;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "repair_orders")
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "car_vin", nullable = false)
    private Car car;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Job> jobs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "final_cost", precision = 12, scale = 2)
    private BigDecimal finalCost;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;

    protected RepairOrder() {
    }

    public RepairOrder(Car car) {
        if (car == null) {
            throw new CarServiceException("Car can't be null.");
        }

        this.car = car;
        this.status = OrderStatus.CREATED;
    }

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getFinalCost() {
        return finalCost;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public void addJob(Job job) {
        if (job == null) {
            throw new CarServiceException("Job can't be null.");
        }

        if (status != OrderStatus.CREATED
                && status != OrderStatus.DIAGNOSED) {
            throw new CarServiceException(
                    "Editing job list is only possible in CREATED or DIAGNOSED status."
            );
        }

        jobs.add(job);

        job.setOrder(this);
    }

    public void diagnose() {
        if (status != OrderStatus.CREATED) {
            throw new CarServiceException(
                    "Can be DIAGNOSED only in CREATED status."
            );
        }

        this.status = OrderStatus.DIAGNOSED;
    }

    public void approve() {
        if (status != OrderStatus.CREATED
                && status != OrderStatus.DIAGNOSED) {
            throw new CarServiceException(
                    "New and diagnosed only can be approved"
            );
        }

        if (jobs.isEmpty()) {
            throw new CarServiceException("No job can be approved");
        }

        this.status = OrderStatus.APPROVED;
    }

    public void startExecution() {
        if (status != OrderStatus.APPROVED) {
            throw new CarServiceException(
                    "Beginning the execution is only possible in APPROVED status."
            );
        }

        this.status = OrderStatus.IN_PROGRESS;
    }

    public void startWork() {
        startExecution();
    }

    public void complete() {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new CarServiceException(
                    "Completion is only possible in IN_PROGRESS status."
            );
        }

        this.status = OrderStatus.COMPLETED;
    }

    public void complete(BigDecimal finalCost) {
        if (status != OrderStatus.IN_PROGRESS
                && status != OrderStatus.APPROVED) {
            throw new CarServiceException(
                    "IN_PROGRESS or APPROVED status only"
            );
        }

        if (finalCost == null || finalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new CarServiceException("Invalid final cost");
        }

        this.status = OrderStatus.COMPLETED;
        this.finalCost = finalCost;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new CarServiceException(
                    "Unable to cancel completed order"
            );
        }

        this.status = OrderStatus.CANCELLED;
    }

    public BigDecimal calculateBaseCost() {
        return jobs.stream()
                .map(Job::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}