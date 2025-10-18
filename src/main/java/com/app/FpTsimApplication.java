package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com"})
@EntityScan(basePackages = {"com.model"})
@EnableJpaRepositories(basePackages = {"com.repository"})
public class FpTsimApplication {
    public static void main(String[] args) {
        SpringApplication.run(FpTsimApplication.class, args);
    }
}
