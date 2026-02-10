package org.example.sbdbaspectscourse;

import org.springframework.boot.SpringApplication;

public class TestSbDbAspectsCourseApplication {

	public static void main(String[] args) {
		SpringApplication.from(SbDbAspectsCourseApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
