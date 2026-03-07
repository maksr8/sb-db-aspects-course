ALTER TABLE vehicles
    ADD CONSTRAINT uk_vehicles_license_plate UNIQUE (license_plate);