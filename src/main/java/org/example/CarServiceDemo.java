package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CarServiceDemo implements CommandLineRunner {

    private final RepairOrderService service;

    public CarServiceDemo(RepairOrderService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {

        System.out.println("=== Autoservice ===\n");

        Client client = new Client("Oleh", "+380991234567");

        String vin = UUID.randomUUID().toString();

        Car car = new Car(
                vin,
                "Toyota",
                "Corolla",
                2002,
                client
        );

        RepairOrder order = service.createOrder(car);

        Long orderId = order.getId();

        System.out.println(
                "Created order №" + orderId
                        + ", Status: " + order.getStatus()
        );

        service.addJobToOrder(
                orderId,
                new Job("Oil change", new BigDecimal("600.00"))
        );

        service.diagnoseOrder(orderId);
        System.out.println("After diagnostics, Status: "
                + service.getOrder(orderId).getStatus());

        service.approveOrder(orderId);
        System.out.println("After approving, Status: "
                + service.getOrder(orderId).getStatus());

        service.startOrderExecution(orderId);
        System.out.println("In progress, Status: "
                + service.getOrder(orderId).getStatus());

        service.completeOrder(orderId);
        System.out.println("Completed, Status: "
                + service.getOrder(orderId).getStatus());

        BigDecimal finalPrice = service.checkout(orderId);

        System.out.println("Total cost: " + finalPrice + " грн\n");

        try {
            service.approveOrder(orderId);
        } catch (CarServiceException e) {
            System.out.println("[Expected error] " + e.getMessage());
        }
    }
}