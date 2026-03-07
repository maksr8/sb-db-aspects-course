package org.example.sbdbaspectscourse.dao;

import lombok.RequiredArgsConstructor;
import org.example.sbdbaspectscourse.model.mongo.Passenger;
import org.example.sbdbaspectscourse.model.mongo.Trip;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MongoTemplateDao {

    private final MongoTemplate mongoTemplate;

    public List<Passenger> findPassengersWithPhoneAndNameLike(String regex) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("phoneNumber").exists(true).ne(""),
                Criteria.where("firstName").regex(regex, "i")
        ));
        return mongoTemplate.find(query, Passenger.class);
    }

    public long updateTripsStatus(String oldStatus, String newStatus) {
        Query query = new Query(Criteria.where("status").is(oldStatus));
        Update update = new Update().set("status", newStatus);
        return mongoTemplate.updateMulti(query, update, Trip.class).getModifiedCount();
    }

    public Double calculateTotalCostForDriverTrips(String driverId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("driverId").is(driverId).and("status").is("COMPLETED")),
                Aggregation.group("driverId").sum("cost").as("totalCost")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "trips", Map.class);
        if (results.getMappedResults().isEmpty()) {
            return 0.0;
        }
        return Double.valueOf(results.getMappedResults().getFirst().get("totalCost").toString());
    }
}