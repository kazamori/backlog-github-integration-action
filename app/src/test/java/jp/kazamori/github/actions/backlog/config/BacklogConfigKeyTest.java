package jp.kazamori.github.actions.backlog.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BacklogConfigKeyTest {
    @Test
    void testName() {
        assertEquals("backlog.apiKey", BacklogConfigKey.API_KEY.get());
        assertEquals("backlog.fqdn", BacklogConfigKey.FQDN.get());
        assertEquals("backlog.projectKey", BacklogConfigKey.PROJECT_KEY.get());
    }
}
