CREATE TABLE raw_feedbacks
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    rating     INT    NOT NULL CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT fk_raw_feedback_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);

INSERT INTO raw_feedbacks (vehicle_id, rating)
VALUES (1, 5),
       (1, 3),
       (1, 4), -- avg 4 for car
       (2, 5),
       (2, 5);