package dev.duran.web_educAlba_backend;

import org.springframework.boot.SpringApplication;

public class TestWebEducAlbaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(WebEducAlbaBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
