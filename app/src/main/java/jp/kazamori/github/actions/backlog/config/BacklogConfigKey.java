package jp.kazamori.github.actions.backlog.config;

public enum BacklogConfigKey implements ConfigKey {
    FQDN,
    API_KEY,
    PROJECT_KEY;

    @Override
    public String getPrefix() {
        return "backlog";
    }
}
