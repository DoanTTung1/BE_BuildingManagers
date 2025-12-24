package com.example.buildingmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BuildingManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuildingManagerApplication.class, args);
    }

}
