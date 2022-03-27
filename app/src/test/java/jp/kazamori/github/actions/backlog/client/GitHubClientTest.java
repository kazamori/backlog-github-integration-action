package jp.kazamori.github.actions.backlog.client;

import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.AppConst;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class GitHubClientTest {

    private static final String PROJECT_KEY = "TEST";
    private static GitHubClient client;

    @BeforeAll
    static void setup() throws IOException {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        val rawClient = new GitHubBuilder().build();
        client = new GitHubClient(config, rawClient);
    }

    static Stream<Arguments> makeMessageData() {
        return Stream.of(
                arguments("", List.of()),
                arguments("no project kye", List.of()),
                arguments("refs #TEST-33 add", List.of("TEST-33")),
                arguments("add TEST-4 and #TEST-8 and TEST-12", List.of("TEST-4", "TEST-8", "TEST-12")),
                arguments("add some feature\n - fix TEST-5435\n - refs TEST-15832", List.of("TEST-5435", "TEST-15832"))
        );
    }

    @ParameterizedTest
    @MethodSource("makeMessageData")
    void searchIssueIds(String message, List<String> expected) {
        val ids = client.searchIssueIds(Arrays.asList(message), PROJECT_KEY);
        if (expected.isEmpty()) {
            assertTrue(ids.isEmpty());
        } else {
            for (var expectedId : expected) {
                assertTrue(ids.contains(expectedId));
            }
        }
    }
}
