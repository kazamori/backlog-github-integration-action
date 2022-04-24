package jp.kazamori.github.actions.backlog.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.api.option.AddIssueCommentParams;
import com.nulabinc.backlog4j.api.option.UpdateIssueParams;
import jp.kazamori.github.actions.backlog.client.BacklogClientUtil;
import jp.kazamori.github.actions.backlog.client.GitHubClient;
import jp.kazamori.github.actions.backlog.config.AppConst;
import jp.kazamori.github.actions.backlog.config.ConfigUtil;
import jp.kazamori.github.actions.backlog.constant.SubCommand;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommit;
import jp.kazamori.github.actions.backlog.exception.UpdateIssueException;
import lombok.SneakyThrows;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Command(name = SubCommand.PUSH, description = "push event that trigger workflows")
public class Push implements Runnable {
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String PUSH_CREATED = "push.created";

    private final Logger logger = LoggerFactory.getLogger(Push.class);

    private final BacklogClient backlogClient;
    private final GitHubClient githubClient;

    public Push(BacklogClient backlogClient, GitHubClient githubClient) {
        this.backlogClient = backlogClient;
        this.githubClient = githubClient;
    }

    @Option(names = "--repository",
            required = true,
            description = "set repository (owner/repo) in the event")
    private String repository;

    @Option(names = "--pusher",
            required = true,
            description = "set pusher in the event")
    private String pusher;

    @Option(names = "--commits",
            required = true,
            description = "set commits in the event")
    private String commits;

    private void addIssueComment(String issueId, List<String> links) {
        val issueCommentParams = new AddIssueCommentParams(issueId, String.join("\n", links));
        val comment = this.backlogClient.addIssueComment(issueCommentParams);
        logger.info(" * comment id: {}", comment.getId());
    }

    private boolean updateIssues(Locale locale, BacklogClientUtil util, List<PushEventCommit> allCommits) {
        var noErrors = true;
        val bundle = ResourceBundle.getBundle(AppConst.BUNDLE_MESSAGES, locale);
        val pusherCreated = String.format(bundle.getString(PUSH_CREATED), this.pusher);
        for (val commitInfo : this.githubClient.getCommitsGroupByIssue(allCommits)) {
            val links = commitInfo.getCommits().stream()
                    .map(PushEventCommit::makeLink)
                    .collect(Collectors.toList());
            links.addAll(0, List.of(pusherCreated, ""));
            try {
                this.addIssueComment(commitInfo.getIssueId(), links);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                noErrors = false;
            }
            val optStatus = commitInfo.getStatus();
            if (optStatus.isPresent()) {
                val params = new UpdateIssueParams(commitInfo.getIssueId()).status(optStatus.get());
                try {
                    val updated = this.backlogClient.updateIssue(params);
                    logger.info("Completed to update issue: {}", updated.getId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    noErrors = false;
                }
            }
        }
        return noErrors;
    }

    @Override
    @SneakyThrows
    public void run() {
        logger.debug("\n{}", this.commits);
        if (this.commits.trim().isEmpty()) {
            return;
        }

        val typeRef = new TypeReference<List<PushEventCommit>>() {
        };
        val pushEventCommits = MAPPER.readValue(this.commits, typeRef);
        if (pushEventCommits.isEmpty()) {
            return;
        }

        val config = this.githubClient.getConfig();
        val locale = ConfigUtil.getLocale(config);
        val util = new BacklogClientUtil(config, this.backlogClient);
        val noErrors = this.updateIssues(locale, util, pushEventCommits);
        if (!noErrors) {
            throw new UpdateIssueException("An error occurred when the client tried to update an issue.");
        }
    }
}
