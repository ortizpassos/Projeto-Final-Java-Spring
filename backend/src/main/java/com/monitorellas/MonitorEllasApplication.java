package com.monitorellas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MonitorEllasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorEllasApplication.class, args);
        System.out.println("API + WebSocket rodando na porta 3000");
    }
}
