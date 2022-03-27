package jp.kazamori.github.actions.backlog.client;

import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.AppConst;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BacklogClientUtilTest {

    private static BacklogClientUtil util;

    @BeforeAll
    static void setup() throws IOException {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        util = new BacklogClientUtil(config, null);
    }

    static Stream<Arguments> makeCommentData() {
        return Stream.of(
                arguments("* コメント", "* コメント", true),
                arguments("* メッセージ\nなにか\n* コメント", "* コメント", true),
                arguments("コメント", "* コメント", false),
                arguments("* メッセージ\nなにか\nコメント", "* コメント", false)
        );
    }

    @ParameterizedTest
    @MethodSource("makeCommentData")
    void hasSameValue(String currentValue, String addValue, boolean expected) {
        val actual = util.hasSameValue(currentValue, addValue);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> makeLastLineData() {
        return Stream.of(
                arguments("* コメント", 1),
                arguments("* コメント\n", 0),
                arguments(" * コメント", 1),
                arguments("- コメント", 1),
                arguments(" - コメント", 1),
                arguments(" - コメント\n", 0),
                arguments("コメント", 2),
                arguments("a - bdd", 2),
                arguments("a - bdd\n", 2),
                arguments("コメント * テスト\n", 2),
                arguments("コメント - テスト", 2),
                arguments("\n", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("makeLastLineData")
    void getLineBreak(String lastLine, int numberOfLineBreak) {
        val actual = util.getLineBreak(Optional.ofNullable(lastLine));
        val expected = "\n".repeat(numberOfLineBreak);
        assertEquals(expected, actual);
    }
}
