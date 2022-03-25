package jp.kazamori.github.actions.backlog.cli;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.client.GitHubClient;
import jp.kazamori.github.actions.backlog.config.AppConst;
import jp.kazamori.github.actions.backlog.config.ConfigUtil;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "Github client debug cli",
        description = "run github client for debugging")
public class GitHubClientCli implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(GitHubClientCli.class);
    private final Config config;

    public GitHubClientCli(Config config) {
        this.config = config;
    }

    @CommandLine.Option(names = "--repository",
            required = true,
            description = "set repository (owner/repo) that the pull request was created")
    private String repository;

    @CommandLine.Option(names = "--pr-number",
            required = true,
            description = "set number of the pull request was created")
    private int prNumber;

    @Override
    public void run() {
        val locale = ConfigUtil.getLocale(this.config);
        val client = GitHubClient.create(this.config);
        val info = client.getIssueIds(this.repository, this.prNumber);
        logger.info(" * link: {}", info.makeLink());
        logger.info(" * comment: {}", info.makeComment(locale));
    }

    public static void main(String[] args) {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        ConfigUtil.setLogLevel(config);
        val exitCode = new CommandLine(new GitHubClientCli(config)).execute(args);
        System.exit(exitCode);
    }
}
