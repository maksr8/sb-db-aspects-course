package org.example.sbdbaspectscourse.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String id;
    private String email;
    private String fullName;
    private String preferences;
}