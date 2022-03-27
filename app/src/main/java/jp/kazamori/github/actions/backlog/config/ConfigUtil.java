package jp.kazamori.github.actions.backlog.config;

import com.typesafe.config.Config;
import lombok.val;

import java.util.Locale;

public class ConfigUtil {

    public static Locale getLocale(Config config) {
        val value = config.getString(AppConfigKey.LOCALE.get());
        val values = value.split("_");
        if (values.length != 2) {
            throw new IllegalStateException(String.format("Wrong format: %s", value));
        }
        return new Locale(values[0], values[1]);
    }

    public static void setLogLevel(Config config) {
        val level = config.getString(AppConfigKey.LOG_LEVEL.get());
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, level.toUpperCase());
    }
}
