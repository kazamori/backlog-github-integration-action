package jp.kazamori.github.actions.backlog.client;

import com.nulabinc.backlog4j.Issue;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.AppConst;
import jp.kazamori.github.actions.backlog.config.BacklogConfigKey;
import jp.kazamori.github.actions.backlog.entity.CommitInfo;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommit;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommitTest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
                arguments("add TEST-4 and #TEST-8 and TEST-12", List.of("TEST-4", "TEST-8", "TEST-12")),
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
        val actual = client.matchKey(PATTERN, message);
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
        val issueId1 = String.format("%s-1", projectKey);
        val allCommits1 = List.of(
                PushEventCommitTest.create(String.format("%s message0", issueId1)),
                PushEventCommitTest.create(String.format("%s message1", issueId1)));
        val expected1 = List.of(
                new CommitInfo(issueId1, allCommits1, Optional.empty()));

        val issueId2 = String.format("%s-2", projectKey);
        val issueId3 = String.format("%s-3", projectKey);
        val allCommits2 = List.of(
                PushEventCommitTest.create(String.format("fix %s message0", issueId1)),
                PushEventCommitTest.create(String.format("%s message1", issueId2)),
                PushEventCommitTest.create(String.format("%s message2", issueId1)),
                PushEventCommitTest.create(String.format("close %s message3", issueId3)),
                PushEventCommitTest.create(String.format("%s message4", issueId3 + "ANOTHER")),
                PushEventCommitTest.create(String.format("%s message5", "ANOTHER" + issueId3)),
                PushEventCommitTest.create(String.format("%s message6", issueId2)));
        val expected2 = List.of(
                new CommitInfo(
                        issueId1,
                        List.of(allCommits2.get(0), allCommits2.get(2)),
                        Optional.of(Issue.StatusType.Resolved)),
                new CommitInfo(
                        issueId2,
                        List.of(allCommits2.get(1), allCommits2.get(6)),
                        Optional.empty()),
                new CommitInfo(
                        issueId3,
                        List.of(allCommits2.get(3)),
                        Optional.of(Issue.StatusType.Closed)));

        return Stream.of(
                arguments(allCommits1, expected1),
                arguments(allCommits2, expected2)
        );
    }

    @ParameterizedTest
    @MethodSource("makeCommitsData")
    void getCommitsRelatedIssue(List<PushEventCommit> allCommits, List<CommitInfo> expected) {
        val actual = client.getCommitsGroupByIssue(allCommits);
        for (val expectedInfo : expected) {
            val info = actual.stream()
                    .filter(e -> e.getIssueId().equals(expectedInfo.getIssueId()))
                    .collect(Collectors.toList())
                    .get(0);
            assertEquals(expectedInfo.getIssueId(), info.getIssueId());
            assertNotNull(info.getCommits());
            assertFalse(info.getCommits().isEmpty());
            assertEquals(expectedInfo.getCommits().size(), info.getCommits().size());
            assertEquals(expectedInfo.getStatus(), info.getStatus());
            var i = 0;
            for (val expectedCommit : expectedInfo.getCommits()) {
                assertEquals(expectedCommit.getMessage(), info.getCommits().get(i).getMessage());
                i++;
            }
        }
    }

    static Stream<Arguments> makeCommitWithStatusData() {
        return Stream.of(
                arguments("TEST-1", "fixed an issue", Optional.empty()),
                arguments("TEST-1", "fix TEST-1 about an issue", Optional.of(Issue.StatusType.Resolved)),
                arguments("TEST-1", "fixes TEST-1 about an issue", Optional.of(Issue.StatusType.Resolved)),
                arguments("TEST-1", "I fixed TEST-1 about an issue", Optional.of(Issue.StatusType.Resolved)),
                arguments("TEST-2", "I fixed TEST-1 about an issue", Optional.empty()),
                arguments("TEST-2", "I fixed TEST-1 TEST-2 about an issue", Optional.empty()),
                arguments("TEST-2", "fixed TEST-1 and closed TEST-2", Optional.of(Issue.StatusType.Closed)),
                arguments("TEST-2", "fixes TEST-1 and closes TEST-2", Optional.of(Issue.StatusType.Closed)),
                arguments("TEST-2", "fix TEST-1 and close TEST-2", Optional.of(Issue.StatusType.Closed)),
                arguments("TEST-3", "fix TEST-1 and close TEST-2 and TEST-3", Optional.empty())
        );
    }

    @ParameterizedTest
    @MethodSource("makeCommitWithStatusData")
    void getStatusToBeUpdated(String issueId, String message, Optional<Issue.StatusType> expected) {
        val actual = client.getStatusToBeUpdated(issueId, message);
        if (expected.isEmpty()) {
            assertTrue(actual.isEmpty());
        } else {
            assertEquals(expected.get(), actual.get());
        }
    }
}
