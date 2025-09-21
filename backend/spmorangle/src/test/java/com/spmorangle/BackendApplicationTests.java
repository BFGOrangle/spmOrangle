package com.spmorangle;

import com.spmorangle.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.defer-datasource-initialization=true",
    "spring.sql.init.mode=always",
    "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
@Import(TestConfig.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}