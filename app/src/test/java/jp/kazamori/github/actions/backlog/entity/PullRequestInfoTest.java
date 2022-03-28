package jp.kazamori.github.actions.backlog.entity;

import lombok.Value;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Value
public class PullRequestInfoTest {

    static Stream<Arguments> makeCommentData() {
        return Stream.of(
                arguments(
                        "title",
                        "http://localhost/",
                        new Locale("ja", "JP"),
                        "プルリクエストが作成されました。\n\n* [title](http://localhost/)"),
                arguments(
                        "title",
                        "http://localhost/",
                        new Locale("en", "US"),
                        "Pull Request was created.\n\n* [title](http://localhost/)")
        );
    }

    @ParameterizedTest
    @MethodSource("makeCommentData")
    void makeComment(String title, String url, Locale locale, String expected) throws MalformedURLException {
        val info = new PullRequestInfo(title, new URL(url), Set.of());
        val actual = info.makeComment(locale);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> makeLinkData() {
        return Stream.of(
                arguments(
                        "pr title",
                        "http://localhost/",
                        "* [pr title](http://localhost/)"),
                arguments(
                        "[test] pr title",
                        "http://localhost/",
                        "* [\\[test\\] pr title](http://localhost/)"),
                arguments(
                        "Java (programming language)",
                        "https://en.wikipedia.org/wiki/Java_(programming_language)",
                        "* [Java (programming language)](https://en.wikipedia.org/wiki/Java_\\(programming_language\\))")
        );
    }

    @ParameterizedTest
    @MethodSource("makeLinkData")
    void makeLink(String title, String url, String expected) throws MalformedURLException {
        val info = new PullRequestInfo(title, new URL(url), Set.of());
        val actual = info.makeLink();
        assertEquals(expected, actual);
    }
}
