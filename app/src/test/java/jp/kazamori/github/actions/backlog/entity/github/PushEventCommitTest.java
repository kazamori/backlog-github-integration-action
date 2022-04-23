package jp.kazamori.github.actions.backlog.entity.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PushEventCommitTest {
    private final Logger logger = LoggerFactory.getLogger(PushEventCommitTest.class);
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static PushEventCommit create(String message) {
        val zoneOffset = ZoneOffset.ofHours(9);
        val datetime = LocalDateTime.of(2022, 4, 22, 12, 13, 14);
        val timestamp = OffsetDateTime.of(datetime, zoneOffset);
        val author = PushEventCommit.Author.builder()
                .email("user1@example.com")
                .name("Tetsuya Morimoto")
                .username("user1")
                .build();
        val committer = PushEventCommit.Committer.builder()
                .email("user1@example.com")
                .name("Tetsuya Morimoto")
                .username("user1")
                .build();
        return PushEventCommit.builder()
                .author(author)
                .committer(committer)
                .distinct(true)
                .id("xxxxxxxx")
                .message(message)
                .timestamp(timestamp)
                .treeId("yyyyyyyy")
                .url("https://github.com/owner/repo/commit/xxxyyy")
                .build();
    }

    private String serialize(PushEventCommit... commits) throws JsonProcessingException {
        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(List.of(commits));
        logger.info("\n{}", json);
        return json;
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        val commit1 = create("message 1");
        val commit2 = create("message 2");
        val json = this.serialize(commit1, commit2);
        val typeReference = new TypeReference<List<PushEventCommit>>() {
        };
        val commits = mapper.readValue(json, typeReference);
        assertEquals(2, commits.size());
        assertEquals(commit1.getAuthor().getUsername(), commits.get(0).getAuthor().getUsername());
        assertEquals(
                commit1.getTimestamp(),
                commits.get(0).getTimestamp().withOffsetSameInstant(ZoneOffset.ofHours(9)));
        assertEquals(commit2.getId(), commits.get(1).getId());
        assertEquals(commit2.getTreeId(), commits.get(1).getTreeId());
    }

    @Test
    void testEmptyStringDeserialize() throws JsonProcessingException {
        val typeReference = new TypeReference<List<PushEventCommit>>() {
        };
        assertThrows(
                MismatchedInputException.class,
                () -> mapper.readValue("", typeReference)
        );
    }
}
