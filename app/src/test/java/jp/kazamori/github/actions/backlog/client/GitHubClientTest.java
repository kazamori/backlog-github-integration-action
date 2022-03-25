package jp.kazamori.github.actions.backlog.client;

import com.google.common.base.Strings;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.AppConst;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitHubClientTest {

    private static final String PROJECT_KEY = "TEST";
    private static GitHubClient client;

    @BeforeAll
    static void setup() throws IOException {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        val rawClient = new GitHubBuilder().build();
        client = new GitHubClient(config, rawClient);
    }

    @ParameterizedTest
    @CsvSource({
            "no project kye,",
            "refs #TEST-33 add,TEST-33",
            "add TEST-4 and #TEST-8 and TEST-12,TEST-4",
            "add TEST-4 and #TEST-8 and TEST-12,TEST-8",
            "add TEST-4 and #TEST-8 and TEST-12,TEST-12",
            "add some feature: - fix TEST-5435: - refs TEST-15832,TEST-5435",
            "add some feature: - fix TEST-5435: - refs TEST-15832,TEST-15832",
    })
    void searchIssueIds(String message_, String expected) {
        val message = message_.replaceAll(":", "\n");
        val ids = client.searchIssueIds(Arrays.asList(message), PROJECT_KEY);
        if (Strings.isNullOrEmpty(expected)) {
            assertTrue(ids.isEmpty());
        } else {
            assertTrue(ids.contains(expected), expected);
        }
    }
}
