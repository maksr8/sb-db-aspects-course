package org.example.sbdbaspectscourse.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "passengers")
public class Passenger {
    @Id
    private String id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
}