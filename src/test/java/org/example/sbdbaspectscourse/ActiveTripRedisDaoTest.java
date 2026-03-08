package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dao.ActiveTripRedisDao;
import org.example.sbdbaspectscourse.model.redis.ActiveTrip;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.*;

@Import(ActiveTripRedisDao.class)
class ActiveTripRedisDaoTest extends AbstractRedisTestcontainersSetupTest {

    @Autowired
    private ActiveTripRedisDao activeTripRedisDao;

    @Test
    void testSaveAndFindActiveTrip() {
        String id = UUID.randomUUID().toString();
        ActiveTrip trip = new ActiveTrip(id, "driver-123", "passenger-456", "IN_PROGRESS");

        activeTripRedisDao.save(trip);

        Optional<ActiveTrip> found = activeTripRedisDao.findById(id);

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("driver-123", found.get().getDriverId());
        Assertions.assertEquals("IN_PROGRESS", found.get().getStatus());
    }

    @Test
    void testDeleteActiveTrip() {
        String id = UUID.randomUUID().toString();
        ActiveTrip trip = new ActiveTrip(id, "driver-999", "passenger-888", "WAITING");

        activeTripRedisDao.save(trip);
        Assertions.assertTrue(activeTripRedisDao.existsById(id));

        activeTripRedisDao.deleteById(id);

        Assertions.assertFalse(activeTripRedisDao.existsById(id));
        Assertions.assertTrue(activeTripRedisDao.findById(id).isEmpty());
    }

    @Test
    void testUpdateActiveTrip() {
        String id = UUID.randomUUID().toString();
        ActiveTrip trip = new ActiveTrip(id, "driver-1", "passenger-1", "WAITING");
        activeTripRedisDao.save(trip);

        trip.setStatus("IN_PROGRESS");
        activeTripRedisDao.update(trip);

        Optional<ActiveTrip> found = activeTripRedisDao.findById(id);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("IN_PROGRESS", found.get().getStatus());
    }

    @Test
    void testFindAllActiveTrips() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        activeTripRedisDao.save(new ActiveTrip(id1, "d-1", "p-1", "STATUS_1"));
        activeTripRedisDao.save(new ActiveTrip(id2, "d-2", "p-2", "STATUS_2"));

        List<ActiveTrip> all = activeTripRedisDao.findAll();

        Assertions.assertTrue(all.size() >= 2);
    }

    @Test
    void testPatchActiveTrip() {
        String id = UUID.randomUUID().toString();
        ActiveTrip trip = new ActiveTrip(id, "driver-original", "passenger-original", "CREATED");
        activeTripRedisDao.save(trip);

        Map<String, String> patchData = new HashMap<>();
        patchData.put("status", "ARRIVED");
        patchData.put("driverId", "driver-new");

        activeTripRedisDao.patch(id, patchData);

        Optional<ActiveTrip> patchedTripOptional = activeTripRedisDao.findById(id);

        Assertions.assertTrue(patchedTripOptional.isPresent());
        ActiveTrip patchedTrip = patchedTripOptional.get();

        Assertions.assertEquals("ARRIVED", patchedTrip.getStatus());
        Assertions.assertEquals("driver-new", patchedTrip.getDriverId());
        Assertions.assertEquals("passenger-original", patchedTrip.getPassengerId());
    }
}