package com.xideral.banco.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración de MongoDB.
 * Solo se activa cuando MongoTemplate está disponible.
 */
@Configuration
@ConditionalOnBean(MongoTemplate.class)
@EnableMongoRepositories(basePackages = {
    "com.xideral.banco.notification.repository",
    "com.xideral.banco.batch.repository"
})
public class MongoConfig {
}