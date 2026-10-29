# Car Service Management System (Laboratory Work #2)

## Overview
This project is a console-based Car Service Management System written in Java. As part of Laboratory Work #2, the monolithic logic from Lab #1 was refactored and separated into layered components following SOLID principles and Dependency Injection (DI).

### Key Features & Architecture
* **Domain Model (`RepairOrder`, `Car`, `Job`):** Manages core business rules, order status, and base cost calculations.
* **Application Service (`RepairOrderService`):** Orchestrates business scenarios and manages order creation and approval workflows.
* **Repository (`RepairOrderRepository`, `InMemoryRepairOrderRepository`):** Decouples in-memory data storage (`Map`) from business logic.
* **Pricing Policy (`PricingPolicy`, `StandardPricingPolicy`):** Implements commercial price calculations, including minimum price enforcement and automatic discount thresholds.

---

## Prerequisites
* **Java Development Kit (JDK):** Version 17 or 21 (LTS).
* **Build System:** Apache Maven 3.x.
* **IDE:** IntelliJ IDEA (or any standard Java IDE).

---

## How to Run

### Option 1: IntelliJ IDEA (Recommended)
1. Open the project in **IntelliJ IDEA**.
2. Wait for Maven to import all dependencies (JUnit 5, Mockito).
3. Navigate to `src/main/java/org/example/CarServiceDemo.java`.
4. Click the green **Run** button (`▶`) next to the `main` method.

### Option 2: Terminal (Maven / Maven Wrapper)
Open a terminal in the root directory of the project and execute:

* **Compile the project:**
  ```bash
  mvn clean compile