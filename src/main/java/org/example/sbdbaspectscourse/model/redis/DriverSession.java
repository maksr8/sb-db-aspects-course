package org.example.sbdbaspectscourse.model.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("driver_session")
public class DriverSession {
    @Id
    private String id;
    private String driverId;
    private String deviceIp;
}