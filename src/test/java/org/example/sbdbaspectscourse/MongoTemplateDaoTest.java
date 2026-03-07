package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.dao.MongoTemplateDao;
import org.example.sbdbaspectscourse.model.mongo.Passenger;
import org.example.sbdbaspectscourse.model.mongo.Trip;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Import(MongoTemplateDao.class)
class MongoTemplateDaoTest extends AbstractMongoTestcontainersSetupTest {

    @Autowired
    private MongoTemplateDao mongoTemplateDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Passenger.class);
        mongoTemplate.dropCollection(Trip.class);
    }

    @Test
    void testFindPassengersWithPhoneAndNameLike() {
        Passenger p1 = new Passenger();
        p1.setFirstName("Alexander");
        p1.setPhoneNumber("+1234567");
        mongoTemplate.save(p1);

        Passenger p2 = new Passenger();
        p2.setFirstName("Alex");
        p2.setPhoneNumber("");
        mongoTemplate.save(p2);

        Passenger p3 = new Passenger();
        p3.setFirstName("Bob");
        p3.setPhoneNumber("+9876543");
        mongoTemplate.save(p3);

        List<Passenger> result = mongoTemplateDao.findPassengersWithPhoneAndNameLike("Alex");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Alexander", result.getFirst().getFirstName());
    }

    @Test
    void testUpdateTripsStatus() {
        Trip t1 = new Trip();
        t1.setStatus("ACTIVE");
        mongoTemplate.save(t1);

        Trip t2 = new Trip();
        t2.setStatus("ACTIVE");
        mongoTemplate.save(t2);

        Trip t3 = new Trip();
        t3.setStatus("COMPLETED");
        mongoTemplate.save(t3);

        long updatedCount = mongoTemplateDao.updateTripsStatus("ACTIVE", "CANCELLED");

        Assertions.assertEquals(2, updatedCount);

        long remainingActive = mongoTemplate.count(new Query(Criteria.where("status").is("ACTIVE")), Trip.class);
        Assertions.assertEquals(0, remainingActive);
    }

    @Test
    void testCalculateTotalCostForDriverTrips() {
        Trip t1 = new Trip();
        t1.setDriverId("D1");
        t1.setStatus("COMPLETED");
        t1.setCost(15.5);
        mongoTemplate.save(t1);

        Trip t2 = new Trip();
        t2.setDriverId("D1");
        t2.setStatus("COMPLETED");
        t2.setCost(20.0);
        mongoTemplate.save(t2);

        Trip t3 = new Trip();
        t3.setDriverId("D1");
        t3.setStatus("ACTIVE");
        t3.setCost(50.0);
        mongoTemplate.save(t3);

        Double total = mongoTemplateDao.calculateTotalCostForDriverTrips("D1");

        Assertions.assertEquals(35.5, total);
    }
}