package org.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepairOrder {
    private static int idCounter = 1;
    private int id;
    private Car car;
    private List<Job> jobs = new ArrayList<>();
    private OrderStatus status;

    public RepairOrder(Car car) {
        if (car == null) {
            throw new CarServiceException("Car can't be null.");
        }
        this.id = idCounter++;
        this.car = car;
        this.status = OrderStatus.CREATED;
    }

    public int getId() {
        return id;
    }
    public Car getCar() {
        return car;
    }
    public OrderStatus getStatus() {
        return status;
    }

    public List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public void addJob(Job job) {
        if (job == null) {
            throw new CarServiceException("Job can't be null.");
        }
        if (status != OrderStatus.CREATED && status != OrderStatus.DIAGNOSED) {
            throw new CarServiceException("Editing job list is only possible in CREATED or DIAGNOSED status.");
        }
        jobs.add(job);
    }


    public void startExecution() {
        if (status != OrderStatus.APPROVED) {
            throw new CarServiceException("Beginning the execution is only possible in APPROVED status.");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new CarServiceException("Completion is only possible in IN_PROGRESS status.");
        }
        this.status = OrderStatus.COMPLETED;
    }



    public BigDecimal calculateBaseCost() {
        BigDecimal total = BigDecimal.ZERO;
        for (Job job : jobs) {
            total = total.add(job.getCost());
        }
        return total;
    }

    public void diagnose() {
        if (status != OrderStatus.CREATED) {
            throw new CarServiceException("Can be DIAGNOSED only in CREATED status.");
        }
        this.status = OrderStatus.DIAGNOSED;
    }

    public void approve() {
        if (status != OrderStatus.CREATED && status != OrderStatus.DIAGNOSED) {
            throw new CarServiceException("New and diagnosed only can be approved");
        }
        if (jobs.isEmpty()) {
            throw new CarServiceException("No job can be approved");
        }
        this.status = OrderStatus.APPROVED;
    }

    public void startWork() {
        if (status != OrderStatus.APPROVED) {
            throw new CarServiceException("Only approved orders can start work");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new CarServiceException("Unable to cancel completed order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void complete(BigDecimal finalCost) {
        if (status != OrderStatus.IN_PROGRESS && status != OrderStatus.APPROVED) {
            throw new CarServiceException("IN_PROGRESS or APPROVED status only");
        }
        this.status = OrderStatus.COMPLETED;
        this.finalCost = finalCost;
        this.completedAt = LocalDateTime.now();
    }
}