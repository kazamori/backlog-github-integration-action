package jp.kazamori.github.actions.backlog.client;

import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.AppConst;
import jp.kazamori.github.actions.backlog.config.BacklogConfigKey;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommit;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommitTest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class GitHubClientTest {

    private static String projectKey;
    private static GitHubClient client;

    @BeforeAll
    static void setup() throws IOException {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        projectKey = config.getString(BacklogConfigKey.PROJECT_KEY.get());
        val rawClient = new GitHubBuilder().build();
        client = new GitHubClient(config, rawClient);
    }

    static Stream<Arguments> makeMessageData() {
        return Stream.of(
                arguments("", List.of()),
                arguments("no project kye", List.of()),
                arguments("add TEST-4 and #TEST-8 and TEST-12", List.of("TEST-4", "TEST-12")),
                arguments("add some feature\n - fix TEST-5435\n - refs TEST-15832", List.of("TEST-5435", "TEST-15832")),
                arguments("ATEST-1 and ATESTB-2 and TESTB-3", List.of()),
                arguments("_TEST-1 and $TEST-2 and %TEST-3", List.of()),
                arguments(" TEST-1 and aTEST-2 and 6TEST-3", List.of("TEST-1"))
        );
    }

    private static Pattern PATTERN = Pattern.compile(
            String.format(GitHubClient.ISSUE_IDS_TEMPLATE, "TEST"),
            Pattern.MULTILINE);

    @ParameterizedTest
    @MethodSource("makeMessageData")
    void matchKey(String message, List<String> expected) {
        val actual = client.matchKey(PATTERN, message, "TEST");
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("makeMessageData")
    void searchIssueIds(String message, List<String> expected) {
        val ids = client.searchIssueIds(Arrays.asList(message), "TEST");
        if (expected.isEmpty()) {
            assertTrue(ids.isEmpty());
        } else {
            for (var expectedId : expected) {
                assertTrue(ids.contains(expectedId));
            }
        }
    }

    static Stream<Arguments> makeCommitsData() {
        val allCommits1 = List.of(
                PushEventCommitTest.create(String.format("%s-1 message0", projectKey)),
                PushEventCommitTest.create(String.format("%s-1 message1", projectKey)));
        val expected1 = Map.of(
                String.format("%s-1", projectKey), allCommits1);

        val allCommits2 = List.of(
                PushEventCommitTest.create(String.format("%s-1 message0", projectKey)),
                PushEventCommitTest.create(String.format("%s-2 message1", projectKey)),
                PushEventCommitTest.create(String.format("%s-1 message2", projectKey)),
                PushEventCommitTest.create(String.format("%s-3 message3", projectKey)),
                PushEventCommitTest.create(String.format("%s-3 message4", projectKey + "ANOTHER")),
                PushEventCommitTest.create(String.format("%s-3 message5", "ANOTHER" + projectKey)),
                PushEventCommitTest.create(String.format("%s-2 message6", projectKey)));
        val expected2 = Map.of(
                String.format("%s-1", projectKey), List.of(allCommits2.get(0), allCommits2.get(2)),
                String.format("%s-2", projectKey), List.of(allCommits2.get(1), allCommits2.get(6)),
                String.format("%s-3", projectKey), List.of(allCommits2.get(3)));

        return Stream.of(
                arguments(allCommits1, expected1),
                arguments(allCommits2, expected2)
        );
    }

    @ParameterizedTest
    @MethodSource("makeCommitsData")
    void getCommitsRelatedIssue(List<PushEventCommit> allCommits, Map<String, List<PushEventCommit>> expected) {
        val actual = client.getCommitsRelatedIssue(allCommits);
        assertEquals(expected.size(), actual.size());
        for (val entry : expected.entrySet()) {
            val commits = actual.get(entry.getKey());
            assertNotNull(commits);
            assertFalse(commits.isEmpty());
            assertEquals(entry.getValue().size(), commits.size());
            var i = 0;
            for (val expectedCommit : entry.getValue()) {
                assertEquals(expectedCommit.getMessage(), commits.get(i).getMessage());
                i++;
            }
        }
    }
}
