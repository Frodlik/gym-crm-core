package com.gym.crm.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@org.springframework.context.annotation.Configuration
@ComponentScan(basePackages = "com.gym.crm.model")
public class HibernateConfig {
    @Value("${db.driver}")
    private String driverClassName;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username}")
    private String dbUser;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${hibernate.hbm2ddl-auto}")
    private String hibernateHbm2ddlAuto;

    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${hibernate.format_sql}")
    private String hibernateFormatSql;

    @Bean
    public SessionFactory sessionFactory() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", driverClassName);
        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.username", dbUser);
        configuration.setProperty("hibernate.connection.password", dbPassword);

        configuration.setProperty("hibernate.dialect", hibernateDialect);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);
        configuration.setProperty("hibernate.show_sql", hibernateShowSql);
        configuration.setProperty("hibernate.format_sql", hibernateFormatSql);
        configuration.setProperty("hibernate.use_sql_comments", "true");

        configuration.setProperty("hibernate.connection.characterEncoding", "utf8");
        configuration.setProperty("hibernate.connection.CharSet", "utf8");
        configuration.setProperty("hibernate.connection.useUnicode", "true");

        configuration.setProperty("hibernate.connection.pool_size", "10");
        configuration.setProperty("hibernate.current_session_context_class", "thread");

        return configuration.buildSessionFactory();
    }
}