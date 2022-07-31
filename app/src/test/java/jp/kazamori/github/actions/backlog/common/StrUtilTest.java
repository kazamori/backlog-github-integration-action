package jp.kazamori.github.actions.backlog.common;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class StrUtilTest {

    static Stream<Arguments> makeStrWithDoubleQuoteData() {
        return Stream.of(
                arguments("", List.of("")),
                arguments("a b c", List.of("a", "b", "c")),
                arguments("\"a b\" c", List.of("a b", "c")),
                arguments("\"a b\" \"c d e\"", List.of("a b", "c d e")),
                arguments("'\"a b\" \"c d e\"'", List.of("'a b", "c d e'")),
                arguments("' \"a b\" \"c d e\" '", List.of("'", "a b", "c d e", "'"))
        );
    }

    @ParameterizedTest
    @MethodSource("makeStrWithDoubleQuoteData")
    void extractStringWithDoubleQuoteToTokens(String str, List<String> expected) {
        val actual = StrUtil.extractStringWithDoubleQuoteToTokens(str);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    static Stream<Arguments> makeStrWithSingleQuoteData() {
        return Stream.of(
                arguments("", List.of("")),
                arguments("a b c", List.of("a", "b", "c")),
                arguments("\"a b\" c", List.of("\"a", "b\"", "c")),
                arguments("\"a b\" \"c d e\"", List.of("\"a", "b\"", "\"c", "d", "e\"")),
                arguments("'\"a b\" \"c d e\"'", List.of("\"a b\" \"c d e\"")),
                arguments("' \"a b\" \"c d e\" '", List.of(" \"a b\" \"c d e\" ")),
                arguments("' \"a b\" \"c \\\"double d\\\" e\" '", List.of(" \"a b\" \"c \\\"double d\\\" e\" ")),
                arguments(
                        "'[{\"author\":{\"name\":\"\\\"Tetsuya Morimoto\\\"\",\"username\":\"t2y\"}]'",
                        List.of("[{\"author\":{\"name\":\"\\\"Tetsuya Morimoto\\\"\",\"username\":\"t2y\"}]")),
                arguments(
                        "'[{\"author\":{\"name\":\"Tetsuya Morimoto\",\"username\":\"t2y\"}]'",
                        List.of("[{\"author\":{\"name\":\"Tetsuya Morimoto\",\"username\":\"t2y\"}]"))
        );
    }

    @ParameterizedTest
    @MethodSource("makeStrWithSingleQuoteData")
    void extractStringWithSingleQuoteToTokens(String str, List<String> expected) {
        val actual = StrUtil.extractStringWithSingleQuoteToTokens(str);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    static Stream<Arguments> makeStrForPushCommandData() {
        return Stream.of(
                arguments("", List.of("")),
                arguments("--verbose", List.of("--verbose")),
                arguments("--verbose --commits '[]'", List.of("--verbose", "--commits", "[]")),
                arguments("--verbose --commits '[{}, {}]' --repository repo",
                        List.of("--verbose", "--repository", "repo", "--commits", "[{}, {}]")),
                arguments("--verbose --commits '[{\"key\": \"value \\\"double quoted\\\" text\"}]' --repository repo",
                        List.of("--verbose", "--repository", "repo",
                                "--commits", "[{\"key\": \"value \\\"double quoted\\\" text\"}]"))
        );
    }

    @ParameterizedTest
    @MethodSource("makeStrForPushCommandData")
    void extractPushCommandArguments(String str, List<String> expected) {
        val actual = StrUtil.extractPushCommandArguments(str);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }
}
