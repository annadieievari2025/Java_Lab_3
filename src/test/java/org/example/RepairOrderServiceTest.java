package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private PricingPolicy pricingPolicy;

    private RepairOrderService service;

    @BeforeEach
    void setUp() {
        service = new RepairOrderService(repository, pricingPolicy);
    }

    private RepairOrder order() {
        return new RepairOrder(new Car("VIN1", "Toyota", "Corolla", 2002,
                new Client("Oleh", "1")));
    }

    private Job job(long id) {
        Job j = new Job("Job " + id, new BigDecimal("100.00"));
        ReflectionTestUtils.setField(j, "id", id);
        return j;
    }

    private RepairOrder inProgress(Job... jobs) {
        RepairOrder o = order();
        Arrays.stream(jobs).forEach(o::addJob);
        o.approve();
        o.startExecution();
        return o;
    }

    @Test
    void createOrderSavesNewOrder() {
        when(repository.save(any(RepairOrder.class))).thenAnswer(i -> i.getArgument(0));

        RepairOrder created = service.createOrder(order().getCar());

        assertEquals(OrderStatus.CREATED, created.getStatus());
        verify(repository).save(created);
    }

    @Test
    void getOrderThrowsWhenNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        CarServiceException ex = assertThrows(CarServiceException.class,
                () -> service.getOrder(5L));

        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    void getOrderReturnsFoundOrder() {
        RepairOrder o = order();
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        assertSame(o, service.getOrder(1L));
    }

    @Test
    void getAllOrdersDelegatesToRepository() {
        List<RepairOrder> list = List.of(order(), order());
        when(repository.findAllByOrderByIdAsc()).thenReturn(list);

        assertEquals(2, service.getAllOrders().size());
    }

    @Test
    void addJobToOrderAddsAndSaves() {
        RepairOrder o = order();
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        service.addJobToOrder(1L, job(1));

        assertEquals(1, o.getJobs().size());
        verify(repository).save(o);
    }

    @Test
    void addJobsToOrderRejectsNullList() {
        assertThrows(CarServiceException.class, () -> service.addJobsToOrder(1L, null));
        verifyNoInteractions(repository);
    }

    @Test
    void addJobsToOrderRejectsEmptyList() {
        assertThrows(CarServiceException.class, () -> service.addJobsToOrder(1L, List.of()));
        verifyNoInteractions(repository);
    }

    @Test
    void addJobsToOrderAddsAllAndSavesOnce() {
        RepairOrder o = order();
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        service.addJobsToOrder(1L, List.of(job(1), job(2), job(3)));

        assertEquals(3, o.getJobs().size());
        verify(repository, times(1)).save(o);
    }

    @Test
    void addJobsToOrderDoesNotSaveWhenItemIsNull() {
        RepairOrder o = order();
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        assertThrows(CarServiceException.class,
                () -> service.addJobsToOrder(1L, Arrays.asList(job(1), null)));

        verify(repository, never()).save(any());
    }

    @Test
    void diagnoseOrderChangesStatusAndSaves() {
        RepairOrder o = order();
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        service.diagnoseOrder(1L);

        assertEquals(OrderStatus.DIAGNOSED, o.getStatus());
        verify(repository).save(o);
    }

    @Test
    void approveOrderChangesStatusAndSaves() {
        RepairOrder o = order();
        o.addJob(job(1));
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        service.approveOrder(1L);

        assertEquals(OrderStatus.APPROVED, o.getStatus());
        verify(repository).save(o);
    }

    @Test
    void approveOrderWithoutJobsFailsAndDoesNotSave() {
        when(repository.findById(1L)).thenReturn(Optional.of(order()));

        assertThrows(CarServiceException.class, () -> service.approveOrder(1L));

        verify(repository, never()).save(any());
    }

    @Test
    void startOrderExecutionChangesStatusAndSaves() {
        RepairOrder o = order();
        o.addJob(job(1));
        o.approve();
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        service.startOrderExecution(1L);

        assertEquals(OrderStatus.IN_PROGRESS, o.getStatus());
        verify(repository).save(o);
    }

    @Test
    void markJobDoneMarksJobAndSaves() {
        Job j = job(7);
        RepairOrder o = inProgress(j);
        when(repository.findById(1L)).thenReturn(Optional.of(o));

        service.markJobDone(1L, 7L);

        assertTrue(j.isDone());
        verify(repository).save(o);
    }

    @Test
    void markJobDoneFailsForUnknownJob() {
        when(repository.findById(1L)).thenReturn(Optional.of(inProgress(job(7))));

        assertThrows(CarServiceException.class, () -> service.markJobDone(1L, 8L));
        verify(repository, never()).save(any());
    }

    @Test
    void completeOrderUsesPricingPolicyAndSavesResult() {
        Job j = job(1);
        RepairOrder o = inProgress(j);
        j.markDone();
        when(repository.findById(1L)).thenReturn(Optional.of(o));
        when(pricingPolicy.calculateCommercialPrice(o)).thenReturn(new BigDecimal("777.00"));

        service.completeOrder(1L);

        assertEquals(OrderStatus.COMPLETED, o.getStatus());
        assertEquals(0, new BigDecimal("777.00").compareTo(o.getFinalCost()));
        assertNotNull(o.getCompletedAt());
        verify(repository).save(o);
    }

    @Test
    void completeOrderFailsWhenJobsNotDoneAndDoesNotSave() {
        RepairOrder o = inProgress(job(1));
        when(repository.findById(1L)).thenReturn(Optional.of(o));
        when(pricingPolicy.calculateCommercialPrice(o)).thenReturn(new BigDecimal("500.00"));

        assertThrows(CarServiceException.class, () -> service.completeOrder(1L));

        assertEquals(OrderStatus.IN_PROGRESS, o.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void completeOrderFailsForUnknownOrder() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(CarServiceException.class, () -> service.completeOrder(9L));
        verifyNoInteractions(pricingPolicy);
    }

    @Test
    void checkoutRejectedForNewOrder() {
        when(repository.findById(1L)).thenReturn(Optional.of(order()));

        assertThrows(CarServiceException.class, () -> service.checkout(1L));
    }

    @Test
    void checkoutReturnsPriceForApprovedOrder() {
        RepairOrder o = order();
        o.addJob(job(1));
        o.approve();
        when(repository.findById(1L)).thenReturn(Optional.of(o));
        when(pricingPolicy.calculateCommercialPrice(o)).thenReturn(new BigDecimal("123.00"));

        assertEquals(0, new BigDecimal("123.00").compareTo(service.checkout(1L)));
    }
}
