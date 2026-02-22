package com.deepflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class DeepFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepFlowApplication.class, args);
    }
}
