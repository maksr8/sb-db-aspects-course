CREATE TABLE vehicles
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    license_plate VARCHAR(255) NOT NULL,
    status        VARCHAR(50),
    vehicle_type  VARCHAR(31)
);

CREATE TABLE cars
(
    vehicle_id     BIGINT           NOT NULL PRIMARY KEY,
    model          VARCHAR(255)     NOT NULL,
    trunk_capacity DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_vehicle_car FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

CREATE TABLE scooters
(
    vehicle_id    BIGINT  NOT NULL PRIMARY KEY,
    battery_level INTEGER NOT NULL,
    max_speed     INTEGER NOT NULL,
    CONSTRAINT fk_vehicle_scooter FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

INSERT INTO vehicles (license_plate, status, vehicle_type)
VALUES ('AA1234BB', 'AVAILABLE', 'CAR');
INSERT INTO cars (vehicle_id, model, trunk_capacity)
VALUES (1, 'Toyota Camry', 500.0);

INSERT INTO vehicles (license_plate, status, vehicle_type)
VALUES ('SCOOT-99', 'FREE', 'SCOOTER');
INSERT INTO scooters (vehicle_id, battery_level, max_speed)
VALUES (2, 85, 25);