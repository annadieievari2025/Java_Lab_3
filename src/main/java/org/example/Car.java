package org.example;

import jakarta.persistence.*;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @Column(length = 50)
    private String vin;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private int year;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "owner_id", nullable = false)
    private Client owner;

    protected Car() {
    }

    public Car(String vin, String brand, String model,
               int year, Client owner) {

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