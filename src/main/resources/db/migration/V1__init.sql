CREATE TABLE cars (
                      vin VARCHAR(50) PRIMARY KEY
);

CREATE TABLE repair_orders (
                               id BIGSERIAL PRIMARY KEY,
                               car_vin VARCHAR(50) NOT NULL REFERENCES cars(vin),
                               status VARCHAR(20) NOT NULL,
                               final_cost DECIMAL(12, 2),
                               completed_at TIMESTAMP
);

CREATE TABLE jobs (
                      id BIGSERIAL PRIMARY KEY,
                      description VARCHAR(255) NOT NULL,
                      cost DECIMAL(12, 2) NOT NULL,
                      order_id BIGINT NOT NULL REFERENCES repair_orders(id) ON DELETE CASCADE
);