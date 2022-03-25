package jp.kazamori.github.actions.backlog.config;

public enum GitHubConfigKey implements ConfigKey {
    TOKEN;

    @Override
    public String getPrefix() {
        return "github";
    }
}
