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
                arguments(new String[]{"a", "b", "c"}, 3, "b"),
                arguments(new String[]{"a b c"}, 3, "b"),
                arguments(new String[]{"a", "b b", "d"}, 3, "b b")
        );
    }

    @ParameterizedTest
    @MethodSource("makeArgsData")
    void ensureArgumentsIsNotQuoted(String[] args, int expectedLength, String second) {
        val actual = Main.ensureArgumentsIsNotQuoted(args);
        assertEquals(expectedLength, actual.length);
        assertEquals(second, actual[1]);
    }
}
