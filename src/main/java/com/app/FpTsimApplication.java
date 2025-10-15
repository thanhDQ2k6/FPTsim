package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com"}) // quét service, controller, ...
@EnableJpaRepositories(basePackages = "com.repository") // quét repository
@EntityScan(basePackages = "com.model") // quét entity
public class FpTsimApplication {
    public static void main(String[] args) {
        SpringApplication.run(FpTsimApplication.class, args);
    }
}
