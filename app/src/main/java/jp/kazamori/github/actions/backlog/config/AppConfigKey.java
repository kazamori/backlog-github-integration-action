package jp.kazamori.github.actions.backlog.config;

public enum AppConfigKey implements ConfigKey {
    LOCALE,
    LOG_LEVEL;

    @Override
    public String getPrefix() {
        return "app";
    }
}
