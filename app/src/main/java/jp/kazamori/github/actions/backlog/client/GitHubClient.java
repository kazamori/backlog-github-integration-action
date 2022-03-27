package jp.kazamori.github.actions.backlog.client;

import com.typesafe.config.Config;
import jp.kazamori.github.actions.backlog.config.BacklogConfigKey;
import jp.kazamori.github.actions.backlog.config.GitHubConfigKey;
import jp.kazamori.github.actions.backlog.entity.PullRequestInfo;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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

    private static final String ISSUE_IDS_TEMPLATE = "(%s-\\d+)";

    public Set<String> searchIssueIds(List<String> messages, String key) {
        val issueIds = new HashSet<String>();
        val regex = String.format(ISSUE_IDS_TEMPLATE, key);
        val pattern = Pattern.compile(regex, Pattern.MULTILINE);
        for (var message : messages) {
            val m = pattern.matcher(message);
            while (m.find()) {
                issueIds.add(m.group());
            }
        }
        return issueIds;
    }

    @SneakyThrows
    public PullRequestInfo getPullRequestInfo(String repository, int prNumber) {
        val repo = this.client.getRepository(repository);
        val pr = repo.getPullRequest(prNumber);
        val messages = this.getCommitMessages(pr);
        val key = config.getString(BacklogConfigKey.PROJECT_KEY.get());
        val issueIds = this.searchIssueIds(messages, key);
        val info = new PullRequestInfo(pr.getTitle(), pr.getHtmlUrl(), issueIds);
        logger.info(" * info: {}", info);
        return info;
    }

    @SneakyThrows
    public static GitHubClient create(Config config) {
        val token = config.getString(GitHubConfigKey.TOKEN.get());
        val github = new GitHubBuilder().withOAuthToken(token).build();
        return new GitHubClient(config, github);
    }
}
