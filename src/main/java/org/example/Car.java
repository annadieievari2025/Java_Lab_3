package org.example;

public class Car {
    private final String vin;
    private final String brand;
    private final String model;
    private final int year;
    private final Client owner;

    public Car(String vin, String brand, String model, int year, Client owner) {
        if (vin == null || vin.isBlank()) {
            throw new CarServiceException("VIN can't be empty");
        }
        if (owner == null) {
            throw new CarServiceException("Owner can't be null");
        }
        this.vin = vin;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.owner = owner;
    }

    public String getVin() {
        return vin;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public Client getOwner() {
        return owner;
    }
}
