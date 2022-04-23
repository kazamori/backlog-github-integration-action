package jp.kazamori.github.actions.backlog.client;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import jp.kazamori.github.actions.backlog.config.BacklogConfigKey;
import jp.kazamori.github.actions.backlog.config.GitHubConfigKey;
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
    List<String> matchKey(Pattern pattern, String message, String key) {
        val results = new ArrayList<String>();
        val matcher = pattern.matcher(message);
        while (matcher.find()) {
            if (matcher.start() != 0) {
                // FIXME: message="MYPROJ" and key="PROJ"
                val prevCharacter = message.substring(matcher.start() - 1, matcher.start());
                if (!prevCharacter.matches("\\s")) {
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
            issueIds.addAll(this.matchKey(pattern, message, key));
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

    public Map<String, List<PushEventCommit>> getCommitsRelatedIssue(List<PushEventCommit> commits) {
        val result = new HashMap<String, List<PushEventCommit>>();
        val key = this.config.getString(BacklogConfigKey.PROJECT_KEY.get());
        val messages = commits.stream().map(PushEventCommit::getMessage).collect(Collectors.toList());
        val issueIds = this.searchIssueIds(messages, key);
        for (val id : issueIds) {
            result.put(id, new ArrayList<>());
            val regex = String.format(ISSUE_IDS_TEMPLATE, key);
            val pattern = Pattern.compile(regex, Pattern.MULTILINE);
            for (val commit : commits) {
                val matchedIds = this.matchKey(pattern, commit.getMessage(), key);
                if (matchedIds.contains(id)) {
                    result.get(id).add(commit);
                }
            }
        }
        return result;
    }

    @SneakyThrows
    public static GitHubClient create(Config config) {
        val token = config.getString(GitHubConfigKey.TOKEN.get());
        val github = new GitHubBuilder().withOAuthToken(token).build();
        return new GitHubClient(config, github);
    }
}
