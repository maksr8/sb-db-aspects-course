CREATE TABLE vehicle_final_stats (
                                     vehicle_id BIGINT PRIMARY KEY,
                                     total_feedbacks INT NOT NULL,
                                     average_rating DOUBLE PRECISION NOT NULL,
                                     CONSTRAINT fk_final_stats_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

INSERT INTO vehicle_final_stats (vehicle_id, total_feedbacks, average_rating)
SELECT
    f.vehicle_id,
    COUNT(f.id) as total_feedbacks,
    r.average_rating
FROM raw_feedbacks f
         JOIN temp_vehicle_ratings r ON f.vehicle_id = r.vehicle_id
GROUP BY f.vehicle_id, r.average_rating;

DROP TABLE temp_vehicle_ratings;
DROP TABLE raw_feedbacks;