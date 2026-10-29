package org.example;

public class Client {
    private final String name;
    private final String phone;

    public Client(String name, String phone) {
        if (name == null || name.isBlank()) {
            throw new CarServiceException("Client's name can't be null");
        }
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
