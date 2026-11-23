package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientAndCarTest {

    private final Client owner = new Client("Oleh", "+380991234567");

    @Test
    void validClientIsCreated() {
        assertEquals("Oleh", owner.getName());
        assertEquals("+380991234567", owner.getPhone());
    }

    @Test
    void clientPhoneMayBeNull() {
        assertDoesNotThrow(() -> new Client("Oleh", null));
    }

    @Test
    void nullClientNameIsRejected() {
        assertThrows(CarServiceException.class, () -> new Client(null, "1"));
    }

    @Test
    void blankClientNameIsRejected() {
        assertThrows(CarServiceException.class, () -> new Client(" ", "1"));
    }

    @Test
    void validCarIsCreated() {
        Car car = new Car("VIN1", "Toyota", "Corolla", 2002, owner);

        assertEquals("VIN1", car.getVin());
        assertEquals("Toyota", car.getBrand());
        assertEquals("Corolla", car.getModel());
        assertEquals(2002, car.getYear());
        assertSame(owner, car.getOwner());
    }

    @Test
    void nullVinIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Car(null, "T", "C", 2002, owner));
    }

    @Test
    void blankVinIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Car(" ", "T", "C", 2002, owner));
    }

    @Test
    void nullOwnerIsRejected() {
        assertThrows(CarServiceException.class,
                () -> new Car("VIN1", "T", "C", 2002, null));
    }
}
