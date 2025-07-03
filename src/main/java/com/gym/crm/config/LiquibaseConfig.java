package com.gym.crm.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {
    @Value("${liquibase.change-log}")
    private String changeLog;

    @Value("${liquibase.contexts}")
    private String contexts;

    @Value("${liquibase.default-schema}")
    private String defaultSchema;

    @Value("${liquibase.drop-first}")
    private boolean dropFirst;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setContexts(contexts);
        liquibase.setDefaultSchema(defaultSchema);
        liquibase.setDropFirst(dropFirst);

        return liquibase;
    }
}
