package jp.kazamori.github.actions.backlog.entity.github;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jp.kazamori.github.actions.backlog.common.MarkdownUtil;
import jp.kazamori.github.actions.backlog.constant.DateTimeConst;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.val;

import java.time.OffsetDateTime;
import java.util.Optional;

@Value
@Builder
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushEventCommit {

    @Value
    @Builder
    @AllArgsConstructor
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Author {
        private String email;
        private String name;
        private String username;
    }

    @JsonProperty
    private Author author;

    @Value
    @Builder
    @AllArgsConstructor
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Committer {
        private String email;
        private String name;
        private String username;
    }

    private Committer committer;
    private boolean distinct;
    private String id;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeConst.GITHUB_FORMAT)
    private OffsetDateTime timestamp;

    @JsonProperty("tree_id")
    private String treeId;

    private String url;

    @JsonIgnore
    public String makeLink() {
        val shortId = this.getId().substring(0, 6);
        val message = Optional.ofNullable(this.getMessage()).orElse("");
        val firstLine = message.split("\\n")[0];
        val title = firstLine.isEmpty() ? shortId : String.format("%s (%s)", firstLine, shortId);
        return String.format("* %s", MarkdownUtil.makeLink(title, this.getUrl()));
    }
}
