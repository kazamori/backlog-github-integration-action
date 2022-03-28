package jp.kazamori.github.actions.backlog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.client.BacklogClientUtil;
import jp.kazamori.github.actions.backlog.client.GitHubClient;
import jp.kazamori.github.actions.backlog.command.PullRequest;
import jp.kazamori.github.actions.backlog.config.ConfigUtil;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command(name = "Backlog GitHub integration action",
        subcommands = {HelpCommand.class},
        description = "provides functionalities to integrate with Backlog")
public class Main {

    // see also: https://www.baeldung.com/java-split-string-commas#2-guavas-splitter-class
    private static final Pattern doubleQuotePattern = Pattern.compile("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    @VisibleForTesting
    static String[] ensureArgumentsIsNotQuoted(String[] args) {
        // for GitHub Actions
        // it takes inputs as two strings like below
        // "pull_request" "--repository kazamori/backlog-github-integration-action --pr-number 1"
        if (args.length != 2) {
            return args;
        }
        val tokens = Splitter
                .on(doubleQuotePattern)
                .splitToList(args[1])
                .stream()
                .map(i -> i.replaceAll("\"", ""))
                .collect(Collectors.toList());
        tokens.add(0, args[0]);
        return tokens.toArray(new String[]{});
    }

    public static void main(String[] args) {
        val config = ConfigFactory.load();
        ConfigUtil.setLogLevel(config);
        val backlogClient = BacklogClientUtil.createClient(config);
        val githubClient = GitHubClient.create(config);
        val exitCode = new CommandLine(new Main())
                .addSubcommand(new PullRequest(backlogClient, githubClient))
                .execute(ensureArgumentsIsNotQuoted(args));
        System.exit(exitCode);
    }
}
