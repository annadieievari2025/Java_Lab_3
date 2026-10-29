package org.example;

import java.math.BigDecimal;

public class CarServiceDemo {
    public static void main(String[] args) {
        RepairOrderRepository repo = new InMemoryRepairOrderRepository();
        PricingPolicy policy = new StandardPricingPolicy();
        RepairOrderService service = new RepairOrderService(repo, policy);

        System.out.println("=== Autoservice ===\n");
        Car car = new Car("VIN123456789");

        RepairOrder order = service.createOrder(car);
        System.out.println("Created order №" + order.getId() + ", Status: " + order.getStatus());

        service.addJobToOrder(order.getId(), new Job("Oil change", new BigDecimal("600.00")));
        service.diagnoseOrder(order.getId());
        System.out.println("After diagnostics, Status: " + order.getStatus());

        service.approveOrder(order.getId());
        System.out.println("After approving, Status: " + order.getStatus());

        service.startOrderExecution(order.getId());
        System.out.println("In progress, Status: " + order.getStatus());

        service.completeOrder(order.getId());
        System.out.println("Completed, Status: " + order.getStatus());

        BigDecimal finalPrice = service.checkout(order.getId());
        System.out.println("Total cost: " + finalPrice + " грн\n");

        try {
            service.approveOrder(order.getId());
        } catch (CarServiceException e) {
            System.out.println("[Expected error] " + e.getMessage());
        }
    }
}