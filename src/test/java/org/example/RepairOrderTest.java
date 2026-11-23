package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RepairOrderTest {

    private RepairOrder newOrder() {
        return new RepairOrder(new Car("VIN1", "Toyota", "Corolla", 2002,
                new Client("Oleh", "1")));
    }

    private Job job(long id, String cost) {
        Job j = new Job("Job " + id, new BigDecimal(cost));
        ReflectionTestUtils.setField(j, "id", id);
        return j;
    }

    private RepairOrder inProgress(Job... jobs) {
        RepairOrder o = newOrder();
        for (Job j : jobs) {
            o.addJob(j);
        }
        o.approve();
        o.startExecution();
        return o;
    }

    @Test
    void newOrderIsCreatedWithoutPriceAndCompletionTime() {
        RepairOrder o = newOrder();

        assertEquals(OrderStatus.CREATED, o.getStatus());
        assertNull(o.getFinalCost());
        assertNull(o.getCompletedAt());
        assertTrue(o.getJobs().isEmpty());
    }

    @Test
    void nullCarIsRejected() {
        assertThrows(CarServiceException.class, () -> new RepairOrder(null));
    }

    @Test
    void addJobLinksJobToOrder() {
        RepairOrder o = newOrder();
        Job j = job(1, "100.00");

        o.addJob(j);

        assertEquals(1, o.getJobs().size());
        assertSame(o, j.getOrder());
    }

    @Test
    void addNullJobIsRejected() {
        assertThrows(CarServiceException.class, () -> newOrder().addJob(null));
    }

    @Test
    void addJobAllowedInDiagnosed() {
        RepairOrder o = newOrder();
        o.diagnose();

        assertDoesNotThrow(() -> o.addJob(job(1, "100.00")));
    }

    @Test
    void addJobRejectedInApproved() {
        RepairOrder o = newOrder();
        o.addJob(job(1, "100.00"));
        o.approve();

        assertThrows(CarServiceException.class, () -> o.addJob(job(2, "5.00")));
    }

    @Test
    void jobsListIsUnmodifiable() {
        RepairOrder o = newOrder();

        assertThrows(UnsupportedOperationException.class,
                () -> o.getJobs().add(job(1, "1.00")));
    }

    @Test
    void diagnoseMovesCreatedToDiagnosed() {
        RepairOrder o = newOrder();

        o.diagnose();

        assertEquals(OrderStatus.DIAGNOSED, o.getStatus());
    }

    @Test
    void diagnoseTwiceIsRejected() {
        RepairOrder o = newOrder();
        o.diagnose();

        assertThrows(CarServiceException.class, o::diagnose);
    }

    @Test
    void approveWithoutJobsIsRejected() {
        assertThrows(CarServiceException.class, newOrder()::approve);
    }

    @Test
    void approveWithJobsSetsApproved() {
        RepairOrder o = newOrder();
        o.addJob(job(1, "100.00"));

        o.approve();

        assertEquals(OrderStatus.APPROVED, o.getStatus());
    }

    @Test
    void approveTwiceIsRejected() {
        RepairOrder o = newOrder();
        o.addJob(job(1, "100.00"));
        o.approve();

        assertThrows(CarServiceException.class, o::approve);
    }

    @Test
    void startExecutionRequiresApproved() {
        assertThrows(CarServiceException.class, newOrder()::startExecution);
    }

    @Test
    void startExecutionMovesToInProgress() {
        assertEquals(OrderStatus.IN_PROGRESS, inProgress(job(1, "100.00")).getStatus());
    }

    @Test
    void markJobDoneMarksOnlyTheRequestedJob() {
        Job a = job(1, "100.00");
        Job b = job(2, "200.00");
        RepairOrder o = inProgress(a, b);

        o.markJobDone(1L);

        assertTrue(a.isDone());
        assertFalse(b.isDone());
    }

    @Test
    void markJobDoneRejectedWhenNotInProgress() {
        RepairOrder o = newOrder();
        o.addJob(job(1, "100.00"));

        assertThrows(CarServiceException.class, () -> o.markJobDone(1L));
    }

    @Test
    void markJobDoneRejectedForUnknownJob() {
        RepairOrder o = inProgress(job(1, "100.00"));

        assertThrows(CarServiceException.class, () -> o.markJobDone(99L));
    }

    @Test
    void markJobDoneRejectedForNullId() {
        RepairOrder o = inProgress(job(1, "100.00"));

        assertThrows(CarServiceException.class, () -> o.markJobDone(null));
    }

    @Test
    void completeSetsStatusCostAndTime() {
        RepairOrder o = inProgress(job(1, "100.00"));
        o.markJobDone(1L);

        o.complete(new BigDecimal("500.00"));

        assertEquals(OrderStatus.COMPLETED, o.getStatus());
        assertEquals(0, new BigDecimal("500.00").compareTo(o.getFinalCost()));
        assertNotNull(o.getCompletedAt());
    }

    @Test
    void completeRejectedWhenSomeJobsNotDone() {
        RepairOrder o = inProgress(job(1, "100.00"), job(2, "200.00"));
        o.markJobDone(1L);

        CarServiceException ex = assertThrows(CarServiceException.class,
                () -> o.complete(new BigDecimal("500.00")));

        assertTrue(ex.getMessage().contains("1 job"));
        assertEquals(OrderStatus.IN_PROGRESS, o.getStatus());
        assertNull(o.getFinalCost());
        assertNull(o.getCompletedAt());
    }

    @Test
    void completeRejectedFromApproved() {
        RepairOrder o = newOrder();
        o.addJob(job(1, "100.00"));
        o.approve();

        assertThrows(CarServiceException.class,
                () -> o.complete(new BigDecimal("500.00")));
    }

    @Test
    void completeRejectedWithNullCost() {
        RepairOrder o = inProgress(job(1, "100.00"));
        o.markJobDone(1L);

        assertThrows(CarServiceException.class, () -> o.complete(null));
    }

    @Test
    void completeRejectedWithNegativeCost() {
        RepairOrder o = inProgress(job(1, "100.00"));
        o.markJobDone(1L);

        assertThrows(CarServiceException.class,
                () -> o.complete(new BigDecimal("-1")));
    }

    @Test
    void completeTwiceIsRejected() {
        RepairOrder o = inProgress(job(1, "100.00"));
        o.markJobDone(1L);
        o.complete(new BigDecimal("500.00"));

        assertThrows(CarServiceException.class,
                () -> o.complete(new BigDecimal("500.00")));
    }

    @Test
    void cancelWorksForNewOrder() {
        RepairOrder o = newOrder();

        o.cancel();

        assertEquals(OrderStatus.CANCELLED, o.getStatus());
    }

    @Test
    void cancelCompletedOrderIsRejected() {
        RepairOrder o = inProgress(job(1, "100.00"));
        o.markJobDone(1L);
        o.complete(new BigDecimal("500.00"));

        assertThrows(CarServiceException.class, o::cancel);
    }

    @Test
    void baseCostIsSumOfJobs() {
        RepairOrder o = newOrder();
        o.addJob(job(1, "100.50"));
        o.addJob(job(2, "200.25"));

        assertEquals(0, new BigDecimal("300.75").compareTo(o.calculateBaseCost()));
    }

    @Test
    void baseCostOfEmptyOrderIsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(newOrder().calculateBaseCost()));
    }
}
