package jp.kazamori.github.actions.backlog.common;

import com.google.common.base.Splitter;
import jp.kazamori.github.actions.backlog.constant.SubCommand;
import lombok.val;

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
        val s1 = str.substring(0, commitsPosition).trim();
        if (str.length() == quoteEnd) {
            return s1;
        }
        val s2 = str.substring(quoteEnd + 1, str.length()).trim();
        return String.format("%s %s", s1, s2);
    }

    private static int getLastIndexFromBeginning(String str, int quoteStart, String quote) {
        var index = str.indexOf(quote, quoteStart + 1);
        if (index < 0) {
            return index;
        }
        int tmp = 0;
        while (tmp >= 0) {
            index = tmp;
            if (str.length() <= index + 1) {
                return index;
            }
            tmp = str.indexOf(quote, index + 1);
        }
        return index;
    }

    private static final String SINGLE_QUOTE = "'";
    private static final String COMMITS_OPTION = "--commits";

    public static List<String> extractPushCommandArguments(String str) {
        val commitsPosition = str.indexOf(COMMITS_OPTION);
        if (commitsPosition < 0) {
            return extractStringWithDoubleQuoteToTokens(str);
        }
        val quoteStart = str.indexOf(SINGLE_QUOTE, commitsPosition + 1);
        val quoteEnd = getLastIndexFromBeginning(str, quoteStart, SINGLE_QUOTE);
        if (quoteEnd < 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        val json = str.substring(quoteStart + 1, quoteEnd);
        // extract arguments without "--commits"
        val others = extractArgumentsWithoutCommits(str, commitsPosition, quoteEnd);
        val tokens = extractStringWithDoubleQuoteToTokens(others);
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
