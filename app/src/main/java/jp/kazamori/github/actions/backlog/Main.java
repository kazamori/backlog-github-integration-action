package jp.kazamori.github.actions.backlog;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.client.BacklogClientUtil;
import jp.kazamori.github.actions.backlog.client.GitHubClient;
import jp.kazamori.github.actions.backlog.command.PullRequest;
import jp.kazamori.github.actions.backlog.command.Push;
import jp.kazamori.github.actions.backlog.common.StrUtil;
import jp.kazamori.github.actions.backlog.config.ConfigUtil;
import lombok.val;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "Backlog GitHub integration action",
        subcommands = {HelpCommand.class},
        description = "provides functionalities to integrate with Backlog")
public class Main {

    @VisibleForTesting
    static String[] ensureArgumentTokens(String[] args) {
        // for GitHub Actions
        // it takes inputs as two strings like below
        // "pull_request" "--repository kazamori/backlog-github-integration-action --pr-number 1"
        if (args.length != 2) {
            return args;
        }
        val tokens = StrUtil.extractStringToTokens(args[0], args[1]);
        tokens.add(0, args[0]);
        return tokens.toArray(new String[]{});
    }

    public static void main(String[] args) {
        val config = ConfigFactory.load();
        ConfigUtil.setLogLevel(config);
        var logger = LoggerFactory.getLogger(Main.class);
        logger.debug("arguments length: {}", args.length);
        val backlogClient = BacklogClientUtil.createClient(config);
        val githubClient = GitHubClient.create(config);
        val exitCode = new CommandLine(new Main())
                .addSubcommand(new PullRequest(backlogClient, githubClient))
                .addSubcommand(new Push(backlogClient, githubClient))
                .execute(ensureArgumentTokens(args));
        System.exit(exitCode);
    }
}
