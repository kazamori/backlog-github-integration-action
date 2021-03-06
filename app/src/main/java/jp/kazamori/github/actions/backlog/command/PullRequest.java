package jp.kazamori.github.actions.backlog.command;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.Issue;
import com.nulabinc.backlog4j.api.option.AddIssueCommentParams;
import jp.kazamori.github.actions.backlog.client.BacklogClientUtil;
import jp.kazamori.github.actions.backlog.client.GitHubClient;
import jp.kazamori.github.actions.backlog.config.ConfigUtil;
import jp.kazamori.github.actions.backlog.constant.SubCommand;
import jp.kazamori.github.actions.backlog.entity.PullRequestInfo;
import jp.kazamori.github.actions.backlog.exception.UpdateIssueException;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Locale;
import java.util.Optional;

@Command(name = SubCommand.PULL_REQUEST, description = "pull_request event that trigger workflows")
public class PullRequest implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(PullRequest.class);

    private final BacklogClient backlogClient;
    private final GitHubClient githubClient;

    public PullRequest(BacklogClient backlogClient, GitHubClient githubClient) {
        this.backlogClient = backlogClient;
        this.githubClient = githubClient;
    }

    @Option(names = "--repository",
            required = true,
            description = "set repository (owner/repo) that the pull request was created")
    private String repository;

    @Option(names = "--pr-number",
            required = true,
            description = "set number of the pull request was created")
    private int prNumber;

    @CommandLine.Option(names = "--custom-field",
            description = "set custom field name")
    private Optional<String> customField;

    private void updateIssue(BacklogClientUtil util, Issue issue, String link) {
        // update pr link to an intended field
        if (this.customField.isPresent()) {
            val customFieldName = this.customField.get();
            logger.info(" * custom field name: {}", customFieldName);
            util.updateCustomFieldOfIssue(issue, customFieldName, link);
        } else {
            util.updateDescriptionOfIssue(issue, link);
        }
    }

    private boolean updateIssues(Locale locale, BacklogClientUtil util, PullRequestInfo info) {
        var noErrors = true;
        for (var id : info.getIssueIds()) {
            val issue = this.backlogClient.getIssue(id);
            val issueCommentParams = new AddIssueCommentParams(issue.getId(), info.makeComment(locale));
            try {
                val comment = this.backlogClient.addIssueComment(issueCommentParams);
                logger.info(" * comment id: {}", comment.getId());
                this.updateIssue(util, issue, info.makeLink());
                logger.info("Completed to update issue: {}", issue.getId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                noErrors = false;
            }
        }
        return noErrors;
    }

    @Override
    public void run() {
        val config = this.githubClient.getConfig();
        val locale = ConfigUtil.getLocale(config);
        val util = new BacklogClientUtil(config, this.backlogClient);
        val info = this.githubClient.getPullRequestInfo(this.repository, this.prNumber);
        val noErrors = this.updateIssues(locale, util, info);
        if (!noErrors) {
            throw new UpdateIssueException("An error occurred when the client tried to update an issue.");
        }
    }
}
