package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

        System.out.println("--- Orders loaded from the database ---");
        List<RepairOrder> existing = service.getAllOrders();
        if (existing.isEmpty()) {
            System.out.println("(none - first run)");
        }
        existing.stream()
                .map(o -> "Order №" + o.getId()
                        + ", car " + o.getCar().getVin()
                        + ", status " + o.getStatus()
                        + ", jobs: " + o.getJobs().size()
                        + " (done: " + o.getJobs().stream().filter(Job::isDone).count() + ")"
                        + ", final cost: " + o.getFinalCost())
                .forEach(System.out::println);
        System.out.println();

        Client client = new Client("Oleh", "+380991234567");
        Car car = new Car(UUID.randomUUID().toString(),
                "Toyota", "Corolla", 2002, client);

        RepairOrder order = service.createOrder(car);
        Long orderId = order.getId();
        System.out.println("Created order №" + orderId
                + ", Status: " + order.getStatus());

        service.addJobToOrder(orderId,
                new Job("Oil change", new BigDecimal("600.00")));

        service.addJobsToOrder(orderId, List.of(
                new Job("Brake pads", new BigDecimal("1500.00")),
                new Job("Air filter", new BigDecimal("300.00"))));
        System.out.println("After valid batch, jobs: "
                + service.getOrder(orderId).getJobs().size());

        try {
            service.addJobsToOrder(orderId, Arrays.asList(
                    new Job("Should be rolled back", new BigDecimal("999.00")),
                    null));
        } catch (CarServiceException e) {
            System.out.println("[Expected error] " + e.getMessage());
        }
        System.out.println("After failed batch, jobs: "
                + service.getOrder(orderId).getJobs().size()
                + " (unchanged)");

        service.diagnoseOrder(orderId);
        service.approveOrder(orderId);
        service.startOrderExecution(orderId);
        System.out.println("In progress, Status: "
                + service.getOrder(orderId).getStatus());

        try {
            service.completeOrder(orderId);
        } catch (CarServiceException e) {
            System.out.println("[Expected error] " + e.getMessage());
        }
        System.out.println("Status after failed completion: "
                + service.getOrder(orderId).getStatus());

        service.getOrder(orderId).getJobs().stream()
                .map(Job::getId)
                .forEach(jobId -> service.markJobDone(orderId, jobId));

        service.completeOrder(orderId);
        RepairOrder done = service.getOrder(orderId);
        System.out.println("Completed, Status: " + done.getStatus()
                + ", final cost: " + done.getFinalCost() + " грн"
                + ", at " + done.getCompletedAt());

        try {
            service.approveOrder(orderId);
        } catch (CarServiceException e) {
            System.out.println("[Expected error] " + e.getMessage());
        }

        System.out.println("\nRestart the application to see this order loaded from the database.");
    }
}
