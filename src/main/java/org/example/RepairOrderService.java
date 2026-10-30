package org.example;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RepairOrderService {

    private final RepairOrderRepository repository;
    private final PricingPolicy pricingPolicy;

    public RepairOrderService(
            RepairOrderRepository repository,
            PricingPolicy pricingPolicy) {

        this.repository = repository;
        this.pricingPolicy = pricingPolicy;
    }

    @Transactional
    public RepairOrder createOrder(Car car) {
        RepairOrder order = new RepairOrder(car);

        return repository.save(order);
    }

    @Transactional
    public void addJobToOrder(Long orderId, Job job) {
        RepairOrder order = findOrder(orderId);

        order.addJob(job);

        repository.save(order);
    }

    @Transactional
    public void diagnoseOrder(Long orderId) {
        RepairOrder order = findOrder(orderId);

        order.diagnose();

        repository.save(order);
    }

    @Transactional
    public void approveOrder(Long orderId) {
        RepairOrder order = findOrder(orderId);

        order.approve();

        repository.save(order);
    }

    @Transactional
    public void startOrderExecution(Long orderId) {
        RepairOrder order = findOrder(orderId);

        order.startExecution();

        repository.save(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        RepairOrder order = findOrder(orderId);

        order.complete();

        repository.save(order);
    }

    @Transactional(readOnly = true)
    public BigDecimal checkout(Long orderId) {
        RepairOrder order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.APPROVED
                && order.getStatus() != OrderStatus.IN_PROGRESS
                && order.getStatus() != OrderStatus.COMPLETED) {

            throw new CarServiceException(
                    "Costing is only possible in APPROVED, IN_PROGRESS or COMPLETED status."
            );
        }

        return pricingPolicy.calculateCommercialPrice(order);
    }

    private RepairOrder findOrder(Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() ->
                        new CarServiceException(
                                "Repair order not found: " + orderId
                        )
                );
    }
}