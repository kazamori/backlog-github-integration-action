package jp.kazamori.github.actions.backlog.client;

import com.typesafe.config.ConfigFactory;
import jp.kazamori.github.actions.backlog.config.AppConst;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BacklogClientUtilTest {

    private static BacklogClientUtil util;

    @BeforeAll
    static void setup() throws IOException {
        val config = ConfigFactory.load(AppConst.LOCAL_DEV_CONF);
        util = new BacklogClientUtil(config, null);
    }

    @ParameterizedTest
    @CsvSource({
            "* コメント,* コメント,true",
            "* メッセージ:なにか:* コメント,* コメント,true",
            "コメント,* コメント,false",
            "* メッセージ:なにか:コメント,* コメント,false",
    })
    void hasSameValue(String currentValue_, String addValue, boolean expected) {
        val currentValue = currentValue_.replaceAll(":", "\n");
        val actual = util.hasSameValue(currentValue, addValue);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "* コメント,1",
            " * コメント,1",
            "- コメント,1",
            " - コメント,1",
            "コメント,2",
            "a - bdd,2",
            "コメント * テスト,2",
            "コメント - テスト,2",
            ":,0",
    })
    void getLineBreak(String lastLine_, int numberOfLineBreak) {
        val lastLine = lastLine_.replaceAll(":", "\n");
        val actual = util.getLineBreak(Optional.ofNullable(lastLine));
        val expected = "\n".repeat(numberOfLineBreak);
        assertEquals(expected, actual);
    }
}
