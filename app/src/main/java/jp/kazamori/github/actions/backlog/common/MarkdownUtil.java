package jp.kazamori.github.actions.backlog.common;

import lombok.val;

import java.net.URL;

public class MarkdownUtil {

    public MarkdownUtil() {
        throw new IllegalStateException("utility class");
    }

    public static String makeLink(String title, URL url) {
        return makeLink(title, url.toString());
    }

    public static String makeLink(String title, String url) {
        val escapedTitle = title
                .replaceAll("\\[", "\\\\[")
                .replaceAll("\\]", "\\\\]");
        val escapedUrl = url
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        return String.format("[%s](%s)", escapedTitle, escapedUrl);
    }
}
