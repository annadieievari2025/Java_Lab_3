package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RepairOrderServiceIntegrationTest {

    @Autowired
    private RepairOrderService service;

    private Long newOrder() {
        Car car = new Car(UUID.randomUUID().toString(), "Toyota", "Corolla",
                2020, new Client("Test Client", "000"));
        return service.createOrder(car).getId();
    }

    private Long inProgressOrder(String... costs) {
        Long id = newOrder();
        service.addJobsToOrder(id, Arrays.stream(costs)
                .map(c -> new Job("Job " + c, new BigDecimal(c)))
                .toList());
        service.approveOrder(id);
        service.startOrderExecution(id);
        return id;
    }

    private void finishAllJobs(Long orderId) {
        service.getOrder(orderId).getJobs().stream()
                .map(Job::getId)
                .forEach(j -> service.markJobDone(orderId, j));
    }

    @Test
    void addJobsToOrderShouldRollbackIfOneJobIsInvalid() {
        Long orderId = newOrder();
        service.addJobToOrder(orderId, new Job("Initial job", new BigDecimal("500.00")));

        assertThrows(CarServiceException.class,
                () -> service.addJobsToOrder(orderId, Arrays.asList(
                        new Job("Oil change", new BigDecimal("600.00")), null)));

        RepairOrder updated = service.getOrder(orderId);
        assertEquals(1, updated.getJobs().size());
        assertEquals("Initial job", updated.getJobs().get(0).getDescription());
    }

    @Test
    void addJobsToOrderShouldRollbackWhenOrderStatusForbidsEditing() {
        Long orderId = inProgressOrder("600.00");

        assertThrows(CarServiceException.class,
                () -> service.addJobsToOrder(orderId,
                        List.of(new Job("Late job", new BigDecimal("100.00")))));

        assertEquals(1, service.getOrder(orderId).getJobs().size());
    }

    @Test
    void addJobsToOrderShouldSaveAllJobsOnSuccess() {
        Long orderId = newOrder();
        service.addJobsToOrder(orderId, List.of(
                new Job("A", new BigDecimal("100.00")),
                new Job("B", new BigDecimal("200.00"))));

        assertEquals(2, service.getOrder(orderId).getJobs().size());
    }

    @Test
    void completeOrderShouldSavePriceStatusAndTime() {
        Long orderId = inProgressOrder("600.00", "400.00");
        finishAllJobs(orderId);

        service.completeOrder(orderId);

        RepairOrder order = service.getOrder(orderId);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(0, new BigDecimal("1000.00").compareTo(order.getFinalCost()));
        assertNotNull(order.getCompletedAt());
    }

    @Test
    void completeOrderShouldFailAndLeaveNoChangesWhenJobsNotDone() {
        Long orderId = inProgressOrder("600.00", "400.00");
        Long firstJob = service.getOrder(orderId).getJobs().get(0).getId();
        service.markJobDone(orderId, firstJob);

        assertThrows(CarServiceException.class, () -> service.completeOrder(orderId));

        RepairOrder order = service.getOrder(orderId);
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertNull(order.getFinalCost());
        assertNull(order.getCompletedAt());
    }

    @Test
    void completeOrderShouldFailIfNotInProgress() {
        Long orderId = newOrder();
        service.addJobToOrder(orderId, new Job("A", new BigDecimal("600.00")));
        service.approveOrder(orderId);

        assertThrows(CarServiceException.class, () -> service.completeOrder(orderId));
        assertEquals(OrderStatus.APPROVED, service.getOrder(orderId).getStatus());
    }

    @Test
    void invalidTransitionAfterCompletionShouldBeRejected() {
        Long orderId = inProgressOrder("600.00");
        finishAllJobs(orderId);
        service.completeOrder(orderId);

        assertThrows(CarServiceException.class, () -> service.approveOrder(orderId));
        assertThrows(CarServiceException.class, () -> service.completeOrder(orderId));
    }

    @Test
    void orderShouldBeLoadedWithJobsAndStatusFromDatabase() {
        Long orderId = inProgressOrder("600.00", "700.00");

        RepairOrder loaded = service.getAllOrders().stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow();

        assertEquals(OrderStatus.IN_PROGRESS, loaded.getStatus());
        assertEquals(2, loaded.getJobs().size());
        assertNotNull(loaded.getCar().getOwner().getName());
    }

    @Test
    void markJobDoneShouldBePersisted() {
        Long orderId = inProgressOrder("600.00");
        Long jobId = service.getOrder(orderId).getJobs().get(0).getId();

        service.markJobDone(orderId, jobId);

        assertTrue(service.getOrder(orderId).getJobs().get(0).isDone());
    }

    @Test
    void markJobDoneShouldFailForUnknownJob() {
        Long orderId = inProgressOrder("600.00");

        assertThrows(CarServiceException.class, () -> service.markJobDone(orderId, -5L));
    }

    @Test
    void markJobDoneShouldFailBeforeExecutionStarted() {
        Long orderId = newOrder();
        service.addJobToOrder(orderId, new Job("A", new BigDecimal("600.00")));
        Long jobId = service.getOrder(orderId).getJobs().get(0).getId();

        assertThrows(CarServiceException.class, () -> service.markJobDone(orderId, jobId));
    }

    @Test
    void checkoutShouldApplyMinimumPriceForCheapOrder() {
        Long orderId = inProgressOrder("100.00");

        assertEquals(0, new BigDecimal("500.00").compareTo(service.checkout(orderId)));
    }

    @Test
    void completeOrderShouldApplyDiscountForExpensiveOrder() {
        Long orderId = inProgressOrder("6000.00", "6000.00");
        finishAllJobs(orderId);

        service.completeOrder(orderId);

        assertEquals(0, new BigDecimal("10800.00")
                .compareTo(service.getOrder(orderId).getFinalCost()));
    }

    @Test
    void getOrderShouldFailForUnknownId() {
        assertThrows(CarServiceException.class, () -> service.getOrder(-1L));
    }
}
