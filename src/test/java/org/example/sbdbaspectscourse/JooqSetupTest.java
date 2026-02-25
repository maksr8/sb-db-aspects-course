package org.example.sbdbaspectscourse;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.example.sbdbaspectscourse.jooq.Tables.VEHICLES;

@SpringBootTest
class JooqSetupTest extends AbstractTestcontainersSetupTest {

    @Autowired
    private DSLContext dsl;

    @Test
    void testJooqConnectionAndQuery() {
        Integer count = dsl.selectCount()
                .from(VEHICLES)
                .fetchOneInto(Integer.class);

        System.out.println("Vehicles in DB according to jOOQ: " + count);
        Assertions.assertNotNull(count);
    }
}
