package jp.kazamori.github.actions.backlog.entity;

import com.nulabinc.backlog4j.Issue;
import jp.kazamori.github.actions.backlog.entity.github.PushEventCommit;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class CommitInfo {
    private String issueId;
    private List<PushEventCommit> commits;
    private Optional<Issue.StatusType> status;
}
