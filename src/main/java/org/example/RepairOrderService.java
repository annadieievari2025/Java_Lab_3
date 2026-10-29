package org.example;

import java.math.BigDecimal;

public class RepairOrderService {
    private RepairOrderRepository repository;
    private PricingPolicy pricingPolicy;

    public RepairOrderService(RepairOrderRepository repository, PricingPolicy pricingPolicy) {
        this.repository = repository;
        this.pricingPolicy = pricingPolicy;
    }

    public RepairOrder createOrder(Car car) {
        RepairOrder order = new RepairOrder(car);
        repository.save(order);
        return order;
    }

    public void addJobToOrder(int orderId, Job job) {
        RepairOrder order = repository.findById(orderId);
        order.addJob(job);
        repository.save(order);
    }

    public void diagnoseOrder(int orderId) {
        RepairOrder order = repository.findById(orderId);
        order.diagnose();
        repository.save(order);
    }

    public void approveOrder(int orderId) {
        RepairOrder order = repository.findById(orderId);
        order.approve();
        repository.save(order);
    }

    public void startOrderExecution(int orderId) {
        RepairOrder order = repository.findById(orderId);
        order.startExecution();
        repository.save(order);
    }

    public void completeOrder(int orderId) {
        RepairOrder order = repository.findById(orderId);
        order.complete();
        repository.save(order);
    }

    public BigDecimal checkout(int orderId) {
        RepairOrder order = repository.findById(orderId);
        if (order.getStatus() != OrderStatus.APPROVED &&
                order.getStatus() != OrderStatus.IN_PROGRESS &&
                order.getStatus() != OrderStatus.COMPLETED) {
            throw new CarServiceException("Costing is only possible in APPROVED, IN_PROGRESS or COMPLETED status.");
        }
        return pricingPolicy.calculateCommercialPrice(order);
    }
}