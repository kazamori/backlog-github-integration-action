package jp.kazamori.github.actions.backlog.cli;

import com.nulabinc.backlog4j.api.option.AddIssueCommentParams;
import com.nulabinc.backlog4j.api.option.UpdateIssueParams;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.client.BacklogClientUtil;
import jp.kazamori.github.actions.backlog.config.AppConst;
import jp.kazamori.github.actions.backlog.config.ConfigUtil;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(name = "Backlog client debug cli",
        description = "run backlog client for debugging")
public class BacklogClientCli implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(BacklogClientCli.class);
    private final Config config;

    public BacklogClientCli(Config config) {
        this.config = config;
    }

    @CommandLine.Option(names = "--issue-id",
            required = true,
            description = "set issue id associates with the pull request")
    private String issueId;

    @CommandLine.Option(names = "--issue-comment",
            description = "set issue comment")
    private Optional<String> issueComment;

    @CommandLine.Option(names = "--issue-description",
            description = "set issue description")
    private Optional<String> issueDescription;

    @CommandLine.Option(names = "--custom-field",
            description = "set custom field name")
    private Optional<String> customField;

    @Override
    public void run() {
        val client = BacklogClientUtil.createClient(this.config);
        val util = new BacklogClientUtil(this.config, client);
        val issue = client.getIssue(this.issueId);
        logger.info(" * summary: {}", issue.getSummary());

        if (issueComment.isPresent()) {
            val params = new AddIssueCommentParams(this.issueId, this.issueComment.get());
            val comment = client.addIssueComment(params);
            logger.info(" * comment id: {}", comment.getId());
        }

        if (issueDescription.isPresent()) {
            val desc = issue.getDescription();
            val params = new UpdateIssueParams(this.issueId)
                    .description(desc + "\n" + this.issueDescription.get());
            val updated = client.updateIssue(params);
            logger.info("Updated issue description:\n{}", updated.getDescription());
        }

        if (customField.isPresent()) {
            val customFieldName = this.customField.get();
            util.updateCustomFieldOfIssue(issue, customFieldName, this.issueComment.get());
        }
    }

    public static void main(String[] args) {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        ConfigUtil.setLogLevel(config);
        val exitCode = new CommandLine(new BacklogClientCli(config)).execute(args);
        System.exit(exitCode);
    }
}
