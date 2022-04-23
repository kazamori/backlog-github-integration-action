package jp.kazamori.github.actions.backlog;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MainTest {
    static Stream<Arguments> makeArgsData() {
        return Stream.of(
                arguments(new String[]{"subcommand", "a", "b", "c"}, 4, "b"),
                arguments(new String[]{"subcommand", "a b c"}, 4, "b"),
                arguments(new String[]{"subcommand", "a", "b b", "d"}, 4, "b b"),
                arguments(new String[]{"subcommand", "a     bb\t\tc"}, 4, "bb"),
                arguments(new String[]{"pull_request", "a \"b b\" c"}, 4, "b b"),
                arguments(new String[]{"pull_request", "\"a a\", \"b b\" c"}, 4, "b b"),
                arguments(new String[]{"pull_request", "\"a a\", \"b b b\" \"c c\""}, 4, "b b b"),
                arguments(new String[]{"push", "opt '[{\"key\": \"value\"}]'"}, 3, "[{\"key\": \"value\"}]"),
                arguments(new String[]{"push", "'opt param' 'a\nb' '[{\"key\": \"value\"}]'"}, 4, "a\nb")
        );
    }

    @ParameterizedTest
    @MethodSource("makeArgsData")
    void ensureArgumentsIsNotQuoted(String[] args, int expectedLength, String second) {
        val actual = Main.ensureArgumentsIsNotQuoted(args);
        assertEquals(expectedLength, actual.length);
        assertEquals(args[0], actual[0]);
        assertEquals(second, actual[2]);
    }
}
