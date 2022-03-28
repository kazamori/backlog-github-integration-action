package jp.kazamori.github.actions.backlog.entity;

import jp.kazamori.github.actions.backlog.config.AppConst;
import lombok.Value;
import lombok.val;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

@Value
public class PullRequestInfo {
    private final String title;
    private final URL url;
    private final Set<String> issueIds;

    private static final String prCreated = "pr.created";

    public String makeComment(Locale locale) {
        val bundle = ResourceBundle.getBundle(AppConst.BUNDLE_MESSAGES, locale);
        return String.format("%s\n\n%s", bundle.getString(prCreated), this.makeLink());
    }

    public String makeLink() {
        val title = this.title
                .replaceAll("\\[", "\\\\[")
                .replaceAll("\\]", "\\\\]");
        val url = this.url.toString()
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        return String.format("* [%s](%s)", title, url);
    }
}
