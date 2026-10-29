package org.example;

import java.util.HashMap;
import java.util.Map;

public class InMemoryRepairOrderRepository implements RepairOrderRepository {
    private Map<Integer, RepairOrder> database = new HashMap<>();

    @Override
    public void save(RepairOrder order) {
        database.put(order.getId(), order);
    }

    @Override
    public RepairOrder findById(int id) {
        RepairOrder order = database.get(id);
        if (order == null) {
            throw new CarServiceException("Order ID " + id + " is not found!");
        }
        return order;
    }
}