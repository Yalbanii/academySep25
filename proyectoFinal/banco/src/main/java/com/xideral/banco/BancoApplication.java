package com.xideral.banco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.xideral.banco.customer.repository",
    "com.xideral.banco.account.repository"
})
@EnableMongoRepositories(basePackages = {
    "com.xideral.banco.notification.repository"
})
public class BancoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoApplication.class, args);
    }
}