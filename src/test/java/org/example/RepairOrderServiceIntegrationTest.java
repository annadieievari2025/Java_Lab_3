
package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RepairOrderServiceIntegrationTest {

    @Autowired
    private RepairOrderService service;

    @Test
    void addJobsToOrderShouldRollbackIfOneJobIsInvalid() {

        // 1. Створюємо замовлення
        Client client = new Client("Test Client", "000");

        Car car = new Car(
                UUID.randomUUID().toString(),
                "Toyota",
                "Corolla",
                2020,
                client
        );

        RepairOrder order = service.createOrder(car);
        Long orderId = order.getId();

        // 2. Додаємо одну початкову роботу
        service.addJobToOrder(
                orderId,
                new Job("Initial job", new BigDecimal("500.00"))
        );

        // 3. Створюємо пакет із коректною та некоректною роботою
        assertThrows(
                CarServiceException.class,
                () -> service.addJobsToOrder(
                        orderId,
                        Arrays.asList(
                                new Job("Oil change", new BigDecimal("600.00")),
                                null
                        )
                )
        );

        // 4. Повторно завантажуємо замовлення з бази
        RepairOrder updatedOrder = service.getOrder(orderId);

        // 5. Перевіряємо, що залишилася тільки початкова робота
        assertEquals(1, updatedOrder.getJobs().size());

        assertEquals(
                "Initial job",
                updatedOrder.getJobs().get(0).getDescription()
        );
    }
}
