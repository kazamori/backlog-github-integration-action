package jp.kazamori.github.actions.backlog.config;

import com.google.common.base.CaseFormat;

public interface ConfigKey {

    String getPrefix();

    String name();

    default String get() {
        return this.getPrefix() + "." + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name());
    }
}
