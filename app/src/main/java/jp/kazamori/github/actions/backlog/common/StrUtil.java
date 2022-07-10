package jp.kazamori.github.actions.backlog.common;

import com.google.common.base.Splitter;
import jp.kazamori.github.actions.backlog.constant.SubCommand;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StrUtil {

    // see also: https://www.baeldung.com/java-split-string-commas#2-guavas-splitter-class
    private static final Pattern DOUBLE_QUOTE_PATTERN = Pattern.compile("\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    public static List<String> extractStringWithDoubleQuoteToTokens(String str) {
        return Splitter
                .on(DOUBLE_QUOTE_PATTERN)
                .splitToList(str.replaceAll("\n", ""))
                .stream()
                .map(i -> i.replaceAll("\"", ""))
                .collect(Collectors.toList());
    }

    private static final Pattern SINGLE_QUOTE_PATTERN = Pattern.compile("\\s+(?=(?:[^']*'[^']*')*[^']*$)");

    public static List<String> extractStringWithSingleQuoteToTokens(String str) {
        return Splitter
                .on(SINGLE_QUOTE_PATTERN)
                .splitToList(str)
                .stream()
                .map(i -> i.replaceAll("'", ""))
                .collect(Collectors.toList());
    }

    private static String extractArgumentsWithoutCommits(String str, int commitsPosition, int quoteEnd) {
        var s1 = str.substring(0, commitsPosition).trim();
        if (str.length() == quoteEnd) {
            return s1;
        }
        var s2 = str.substring(quoteEnd + 1, str.length()).trim();
        return String.format("%s %s", s1, s2);
    }

    private static final String COMMITS_OPTION = "--commits";

    public static List<String> extractPushCommandArguments(String str) {
        var commitsPosition = str.indexOf(COMMITS_OPTION);
        if (commitsPosition < 0) {
            return extractStringWithDoubleQuoteToTokens(str);
        }
        var quoteStart = str.indexOf("'", commitsPosition + 1);
        var quoteEnd = str.indexOf("'", quoteStart + 1);
        var json = str.substring(quoteStart + 1, quoteEnd);
        // extract arguments without "--commits"
        var others = extractArgumentsWithoutCommits(str, commitsPosition, quoteEnd);
        var tokens = extractStringWithDoubleQuoteToTokens(others);
        tokens.add(COMMITS_OPTION);
        tokens.add(json);
        return tokens.stream().filter(e -> !e.trim().isEmpty()).collect(Collectors.toList());
    }

    public static List<String> extractStringToTokens(String subCommand, String arguments) {
        switch (subCommand) {
            case SubCommand.PUSH:
                return extractPushCommandArguments(arguments);
            default:
                return extractStringWithDoubleQuoteToTokens(arguments);
        }
    }
}
