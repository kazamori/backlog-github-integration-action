package jp.kazamori.github.actions.backlog;

import org.junit.jupiter.api.Test;

class MainTest {
    @Test
    void canShowHelp() {
        Main.main(new String[]{"help"});
    }
}
