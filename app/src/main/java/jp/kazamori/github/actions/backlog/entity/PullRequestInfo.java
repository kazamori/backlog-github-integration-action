package jp.kazamori.github.actions.backlog.entity;

import jp.kazamori.github.actions.backlog.common.MarkdownUtil;
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

    private static final String PR_CREATED = "pr.created";

    public String makeComment(Locale locale) {
        val bundle = ResourceBundle.getBundle(AppConst.BUNDLE_MESSAGES, locale);
        return String.format("%s\n\n%s", bundle.getString(PR_CREATED), this.makeLink());
    }

    public String makeLink() {
        return String.format("* %s", MarkdownUtil.makeLink(title, url));
    }
}
