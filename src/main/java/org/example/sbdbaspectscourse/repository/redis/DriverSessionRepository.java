package org.example.sbdbaspectscourse.repository.redis;

import org.example.sbdbaspectscourse.model.redis.DriverSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverSessionRepository extends CrudRepository<DriverSession, String> {
}