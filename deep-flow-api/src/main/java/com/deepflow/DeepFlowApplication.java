package com.deepflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DeepFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepFlowApplication.class, args);
    }
}
