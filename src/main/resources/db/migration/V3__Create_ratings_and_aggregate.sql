CREATE TABLE temp_vehicle_ratings
(
    vehicle_id     BIGINT PRIMARY KEY,
    average_rating DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_temp_rating_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

INSERT INTO temp_vehicle_ratings (vehicle_id, average_rating)
SELECT vehicle_id, AVG(rating)
FROM raw_feedbacks
GROUP BY vehicle_id;