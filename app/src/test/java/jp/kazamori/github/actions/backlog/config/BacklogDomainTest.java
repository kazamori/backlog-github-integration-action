package jp.kazamori.github.actions.backlog.config;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BacklogDomainTest {
    @Test
    void testGetFromBacklogCom() throws MalformedURLException {
        val config = BacklogDomain.create("kazamori.backlog.com", "myApiKey");
        assertEquals("https://kazamori.backlog.com", config.getWebAppBaseURL());
        assertEquals("myApiKey", config.getApiKey());
    }

    @Test
    void testGetFromBacklogToolCom() throws MalformedURLException {
        val config = BacklogDomain.create("kazamori.backlogtool.com", "myApiKey");
        assertEquals("https://kazamori.backlogtool.com", config.getWebAppBaseURL());
        assertEquals("myApiKey", config.getApiKey());
    }

    @Test
    void testGetFromBacklogJp() throws MalformedURLException {
        val config = BacklogDomain.create("kazamori.backlog.jp", "myApiKey");
        assertEquals("https://kazamori.backlog.jp", config.getWebAppBaseURL());
        assertEquals("myApiKey", config.getApiKey());
    }

    @Test
    void testGetUnsupportedDomain() throws MalformedURLException {
        assertThrows(
                IllegalArgumentException.class,
                () -> BacklogDomain.create("kazamori.another.jp", "myApiKey")
        );
    }
}
