package jp.kazamori.github.actions.backlog.client;

import com.google.common.annotations.VisibleForTesting;
import com.nulabinc.backlog4j.Issue;
import com.typesafe.config.Config;
import jp.kazamori.github.actions.backlog.config.BacklogConfigKey;
import jp.kazamori.github.actions.backlog.config.GitHubConfigKey;
import jp.kazamori.github.actions.backlog.entity.CommitInfo;
import jp.kazamori.github.actions.backlog.entity.PullRequestInfo;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommit;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class GitHubClient {
    private final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private final Config config;
    private final GitHub client;

    public GitHubClient(Config config, GitHub client) {
        this.config = config;
        this.client = client;
    }

    private List<String> getCommitMessages(GHPullRequest pr) {
        val commitMessages = new ArrayList<String>();
        for (var detail : pr.listCommits()) {
            commitMessages.add(detail.getCommit().getMessage());
        }
        return commitMessages;
    }

    @VisibleForTesting
    List<String> matchKey(Pattern pattern, String message) {
        val results = new ArrayList<String>();
        val matcher = pattern.matcher(message);
        while (matcher.find()) {
            if (matcher.start() != 0) {
                // FIXME: message="MYPROJ" and key="PROJ"
                val prevCharacter = message.substring(matcher.start() - 1, matcher.start());
                if (!(prevCharacter.equals("#")  // hash sign (e.g. #PROJ-1) is also valid since it's common
                        || prevCharacter.matches("\\s"))) {
                    continue; // skip
                }
            }
            if (matcher.end() != message.length()) {
                // FIXME: message="PROJ-1SUFFIX" and key="PROJ"
                val nextCharacter = message.substring(matcher.end(), matcher.end() + 1);
                if (!nextCharacter.matches("\\s")) {
                    continue; // skip
                }
            }
            results.add(matcher.group());
        }
        return results;
    }

    @VisibleForTesting
    static final String ISSUE_IDS_TEMPLATE = "(%s-\\d+)";

    public Set<String> searchIssueIds(List<String> messages, String key) {
        val issueIds = new HashSet<String>();
        val regex = String.format(ISSUE_IDS_TEMPLATE, key);
        val pattern = Pattern.compile(regex, Pattern.MULTILINE);
        for (var message : messages) {
            issueIds.addAll(this.matchKey(pattern, message));
        }
        return issueIds;
    }

    @SneakyThrows
    public PullRequestInfo getPullRequestInfo(String repository, int prNumber) {
        val repo = this.client.getRepository(repository);
        val pr = repo.getPullRequest(prNumber);
        val messages = this.getCommitMessages(pr);
        val key = this.config.getString(BacklogConfigKey.PROJECT_KEY.get());
        val issueIds = this.searchIssueIds(messages, key);
        val info = new PullRequestInfo(pr.getTitle(), pr.getHtmlUrl(), issueIds);
        logger.info(" * info: {}", info);
        return info;
    }

    private static final String STATUS_ISSUE_IDS_TEMPLATE = "\\s?(\\w+)\\s%s";

    @VisibleForTesting
    Optional<Issue.StatusType> getStatusToBeUpdated(String issueId, String message) {
        val pattern = Pattern.compile(String.format(STATUS_ISSUE_IDS_TEMPLATE, issueId));
        val matcher = pattern.matcher(message);
        if (matcher.find()) {
            val keyword = matcher.group(1);
            switch (keyword) {
                case "fix":
                case "fixes":
                case "fixed":
                    return Optional.of(Issue.StatusType.Resolved);
                case "close":
                case "closes":
                case "closed":
                    return Optional.of(Issue.StatusType.Closed);
            }
        }
        return Optional.empty();
    }

    private CommitInfo createCommitInfo(List<PushEventCommit> commits, String key, String issueId) {
        val regex = String.format(ISSUE_IDS_TEMPLATE, key);
        val pattern = Pattern.compile(regex, Pattern.MULTILINE);
        var status = Optional.<Issue.StatusType>empty();
        val matchedCommits = new ArrayList<PushEventCommit>();
        for (val commit : commits) {
            val matchedIds = this.matchKey(pattern, commit.getMessage());
            if (matchedIds.contains(issueId)) {
                matchedCommits.add(commit);
                val tmpStatus = this.getStatusToBeUpdated(issueId, commit.getMessage());
                if (tmpStatus.isPresent()) {
                    status = tmpStatus;
                }
            }
        }
        return new CommitInfo(issueId, matchedCommits, status);
    }

    public List<CommitInfo> getCommitsGroupByIssue(List<PushEventCommit> commits) {
        val results = new ArrayList<CommitInfo>();
        val key = this.config.getString(BacklogConfigKey.PROJECT_KEY.get());
        val messages = commits.stream().map(PushEventCommit::getMessage).collect(Collectors.toList());
        val issueIds = this.searchIssueIds(messages, key);
        for (val id : issueIds) {
            results.add(this.createCommitInfo(commits, key, id));
        }
        return results;
    }

    @SneakyThrows
    public static GitHubClient create(Config config) {
        val token = config.getString(GitHubConfigKey.TOKEN.get());
        val github = new GitHubBuilder().withOAuthToken(token).build();
        return new GitHubClient(config, github);
    }
}
