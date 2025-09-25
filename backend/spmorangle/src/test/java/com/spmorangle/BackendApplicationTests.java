package com.spmorangle;

import com.spmorangle.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // with all beans properly configured and no circular dependencies
    }

    @Test
    void applicationStartsSuccessfully() {
        // This test verifies that the application can start up completely
        // without any configuration errors or missing dependencies
        // The fact that this test runs means the application context is healthy
    }
}
