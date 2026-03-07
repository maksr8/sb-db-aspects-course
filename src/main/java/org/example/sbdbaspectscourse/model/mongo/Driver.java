package org.example.sbdbaspectscourse.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "drivers")
public class Driver {
    @Id
    private String id;
    private String fullName;
    private String licenseNumber;
    private Double rating;
    private boolean active;
}