package com.wearewaes.simple_bank_account;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles(value = "test")
public abstract class TestSetup {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.3-alpine")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("S3cret");

    @BeforeAll
    static void setUp() {
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
        System.setProperty("spring.flyway.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.flyway.user", postgreSQLContainer.getUsername());
        System.setProperty("spring.flyway.password", postgreSQLContainer.getPassword());
        System.setProperty("security.encryption.key", "5lyi1fhGSeoBrI0+qERnWBUJmitWJ9IX3GVCYqANmt4=");
    }

    @AfterAll
    static void tearDown() {
        postgreSQLContainer.stop();
    }
}
