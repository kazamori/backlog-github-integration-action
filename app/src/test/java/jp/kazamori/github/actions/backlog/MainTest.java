package jp.kazamori.github.actions.backlog;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MainTest {
    static Stream<Arguments> makePullRequestArgsData() {
        return Stream.of(
                arguments(new String[]{"subcommand", "a", "b", "c"}, 4, "b"),
                arguments(new String[]{"subcommand", "a b c"}, 4, "b"),
                arguments(new String[]{"subcommand", "a", "b b", "d"}, 4, "b b"),
                arguments(new String[]{"subcommand", "a     bb\t\tc"}, 4, "bb"),
                arguments(new String[]{"pull_request", "a \"b b\" c"}, 4, "b b"),
                arguments(new String[]{"pull_request", "\"a a\", \"b b\" c"}, 4, "b b"),
                arguments(new String[]{"pull_request", "\"a\na\", \"b\nb\" c"}, 4, "bb"),
                arguments(new String[]{"pull_request", "\"a a\", \"b b b\" \"c c\""}, 4, "b b b")
        );
    }

    @ParameterizedTest
    @MethodSource("makePullRequestArgsData")
    void ensureArgumentTokensForPullRequest(String[] args, int expectedLength, String second) {
        val actual = Main.ensureArgumentTokens(args);
        assertEquals(expectedLength, actual.length);
        assertEquals(args[0], actual[0]);
        assertEquals(second, actual[2]);
    }

    static Stream<Arguments> makePushArgsData() {
        return Stream.of(
                arguments(new String[]{
                        "push", "--commits '[{\"key\": \"value\"}]' --opt param"}, 5, "[{\"key\": \"value\"}]"),
                arguments(new String[]{
                        "push", "--opt param --commits '[{\"key\": \"handled by \\\"double", "quote\\\"\"}]'"},
                        5, "[{\"key\": \"handled by \\\"double quote\\\"\"}]"),
                arguments(new String[]{"push", "--opt 'param' 'a\nb' --commits '[{\"key\": \"value\"}]' --verbose"},
                        7, "[{\"key\": \"value\"}]")
        );
    }

    @ParameterizedTest
    @MethodSource("makePushArgsData")
    void ensureArgumentTokensForPush(String[] args, int expectedLength, String json) {
        val actual = Main.ensureArgumentTokens(args);
        assertEquals(expectedLength, actual.length);
        assertEquals(args[0], actual[0]);
        assertEquals(json, actual[actual.length - 1]);
    }
}
