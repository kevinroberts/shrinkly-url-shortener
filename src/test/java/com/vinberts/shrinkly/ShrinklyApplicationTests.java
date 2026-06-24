package com.vinberts.shrinkly;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Flyway is disabled here because this smoke test boots the full context
// without provisioning a database; runtime Flyway would otherwise eagerly
// connect to the datasource and fail. Migrations are exercised at deploy time.
@SpringBootTest(properties = "spring.flyway.enabled=false")
public class ShrinklyApplicationTests {

    @Test
    public void contextLoads() {
    }

}
