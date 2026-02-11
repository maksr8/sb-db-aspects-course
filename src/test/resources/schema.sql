CREATE TABLE vehicles (
                          id SERIAL PRIMARY KEY,
                          license_plate VARCHAR(255) NOT NULL,
                          status VARCHAR(50),
                          vehicle_type VARCHAR(31)
);

CREATE TABLE cars (
                      model VARCHAR(255),
                      trunk_capacity DOUBLE PRECISION,
                      vehicle_id BIGINT NOT NULL PRIMARY KEY,
                      CONSTRAINT fk_vehicle_car FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE TABLE scooters (
                          battery_level INTEGER,
                          max_speed INTEGER,
                          vehicle_id BIGINT NOT NULL PRIMARY KEY,
                          CONSTRAINT fk_vehicle_scooter FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);