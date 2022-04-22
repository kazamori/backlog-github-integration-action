package jp.kazamori.github.actions.backlog.common;

import com.google.common.base.Splitter;
import jp.kazamori.github.actions.backlog.constant.SubCommand;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StrUtil {

    // see also: https://www.baeldung.com/java-split-string-commas#2-guavas-splitter-class
    private static final Pattern doubleQuotePattern = Pattern.compile("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    public static List<String> extractStringWithDoubleQuoteToTokens(String str) {
        return Splitter
                .on(doubleQuotePattern)
                .splitToList(str)
                .stream()
                .map(i -> i.replaceAll("\"", ""))
                .collect(Collectors.toList());
    }

    private static final Pattern singleQuotePattern = Pattern.compile("\\s+(?=(?:[^']*'[^']*')*[^']*$)");

    public static List<String> extractStringWithSingleQuoteToTokens(String str) {
        return Splitter
                .on(singleQuotePattern)
                .splitToList(str)
                .stream()
                .map(i -> i.replaceAll("'", ""))
                .collect(Collectors.toList());
    }

    public static List<String> extractStringToTokens(String subCommand, String arguments) {
        switch (subCommand) {
            case SubCommand.PUSH:
                return extractStringWithSingleQuoteToTokens(arguments);
            default:
                return extractStringWithDoubleQuoteToTokens(arguments);
        }
    }
}
