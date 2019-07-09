package com.ddphin.ddphin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class DDphinApplication {

    public static void main(String[] args) {
        SpringApplication.run(DDphinApplication.class, args);
    }
}
