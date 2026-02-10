package org.example.sbdbaspectscourse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class SbDbAspectsCourseApplicationTests {

	@Test
	void contextLoads() {
	}

}
