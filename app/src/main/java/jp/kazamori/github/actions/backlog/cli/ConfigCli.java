package jp.kazamori.github.actions.backlog.cli;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.*;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "Loading config file debug cli",
        description = "load config file for debugging")
public class ConfigCli implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ConfigCli.class);
    private final Config config;

    public ConfigCli(Config config) {
        this.config = config;
    }

    @Override
    public void run() {
        // app
        logger.info(" * app.locale: {}", this.config.getString(AppConfigKey.LOCALE.get()));
        logger.info(" * app.logLevel: {}", this.config.getString(AppConfigKey.LOG_LEVEL.get()));
        // backlog
        logger.info(" * backlog.apiKey: {}", this.config.getString(BacklogConfigKey.API_KEY.get()));
        logger.info(" * backlog.fqdn: {}", this.config.getString(BacklogConfigKey.FQDN.get()));
        logger.info(" * backlog.projectKey: {}", this.config.getString(BacklogConfigKey.PROJECT_KEY.get()));
        // github
        logger.info(" * github.token: {}", this.config.getString(GitHubConfigKey.TOKEN.get()));
        logger.info("");

        // util
        logger.trace(" * trace message");
        logger.debug(" * debug message");
        logger.info(" * info message");
        logger.warn(" * warn message");
        logger.error(" * error message");
        val locale = ConfigUtil.getLocale(this.config);
        logger.info(" * locale: {}", locale);
    }

    public static void main(String[] args) {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        ConfigUtil.setLogLevel(config);
        val exitCode = new CommandLine(new ConfigCli(config)).execute(args);
        System.exit(exitCode);
    }
}
