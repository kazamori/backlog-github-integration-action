package jp.kazamori.github.actions.backlog.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppConfigKeyTest {
    @Test
    void testName() {
        assertEquals("app.locale", AppConfigKey.LOCALE.get());
        assertEquals("app.logLevel", AppConfigKey.LOG_LEVEL.get());
    }
}
