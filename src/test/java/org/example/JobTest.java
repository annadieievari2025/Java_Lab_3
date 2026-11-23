package org.example;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class JobTest {

    @Test
    void validJobIsCreatedNotDone() {
        Job job = new Job("Oil change", new BigDecimal("600.00"));

        assertEquals("Oil change", job.getDescription());
        assertEquals(0, new BigDecimal("600.00").compareTo(job.getCost()));
        assertFalse(job.isDone());
    }

    @Test
    void nullDescriptionIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Job(null, BigDecimal.TEN));
    }

    @Test
    void blankDescriptionIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Job("   ", BigDecimal.TEN));
    }

    @Test
    void nullCostIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Job("Work", null));
    }

    @Test
    void negativeCostIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Job("Work", new BigDecimal("-0.01")));
    }

    @Test
    void zeroCostIsAllowed() {
        assertDoesNotThrow(() -> new Job("Free check", BigDecimal.ZERO));
    }

    @Test
    void markDoneSetsFlag() {
        Job job = new Job("Work", BigDecimal.TEN);

        job.markDone();

        assertTrue(job.isDone());
    }

    @Test
    void markDoneIsIdempotent() {
        Job job = new Job("Work", BigDecimal.TEN);

        job.markDone();
        job.markDone();

        assertTrue(job.isDone());
    }
}
