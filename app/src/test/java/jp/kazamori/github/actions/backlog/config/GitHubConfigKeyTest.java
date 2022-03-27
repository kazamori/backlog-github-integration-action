package jp.kazamori.github.actions.backlog.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitHubConfigKeyTest {
    @Test
    void testName() {
        assertEquals("github.token", GitHubConfigKey.TOKEN.get());
    }
}
