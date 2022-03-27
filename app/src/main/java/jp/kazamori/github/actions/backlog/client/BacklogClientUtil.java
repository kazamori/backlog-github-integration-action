package jp.kazamori.github.actions.backlog.client;

import com.google.common.annotations.VisibleForTesting;
import com.nulabinc.backlog4j.*;
import com.nulabinc.backlog4j.api.option.CustomFiledValue;
import com.nulabinc.backlog4j.api.option.UpdateIssueParams;
import com.nulabinc.backlog4j.internal.json.customFields.TextAreaCustomField;
import com.typesafe.config.Config;
import jp.kazamori.github.actions.backlog.config.BacklogConfigKey;
import jp.kazamori.github.actions.backlog.config.BacklogDomain;
import lombok.SneakyThrows;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Pattern;

public class BacklogClientUtil {
    private final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private final Config config;
    private final BacklogClient client;
    private ResponseList<CustomFieldSetting> customFields = null;

    public BacklogClientUtil(Config config, BacklogClient client) {
        this.config = config;
        this.client = client;
    }

    private ResponseList<CustomFieldSetting> getCustomFields() {
        if (this.customFields == null) {
            val projectKey = this.config.getString(BacklogConfigKey.PROJECT_KEY.get());
            this.customFields = this.client.getCustomFields(projectKey);
        }
        return this.customFields;
    }

    public boolean hasCustomField(String fieldName) {
        val customFields = this.getCustomFields();
        for (var field : customFields) {
            logger.debug("custom field name: {}", field.getName());
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public long getCustomFieldId(String fieldName) {
        val customFields = this.getCustomFields();
        for (var field : customFields) {
            if (field.getName().equals(fieldName)) {
                return field.getId();
            }
        }
        throw new IllegalArgumentException(String.format("Not found in custom fields: %s", fieldName));
    }

    public Optional<CustomField> getCustomFieldOfIssue(Issue issue, String fieldName) {
        val fields = issue.getCustomFields();
        for (var field : fields) {
            if (field.getName().equals(fieldName)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    private boolean hasSameValue(String[] currentValues, String addValue) {
        for (var value : currentValues) {
            if (value.equals(addValue)) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    boolean hasSameValue(String currentValue, String addValue) {
        return this.hasSameValue(currentValue.split("\\n"), addValue);
    }

    private static final Pattern LIST_LINE = Pattern.compile("^(\s*\\*|-\s)");

    @VisibleForTesting
    String getLineBreak(Optional<String> lastLine) {
        if (lastLine.isEmpty()) {
            return "\n";
        }
        val line = lastLine.get();
        if (line.equals("\n")) {
            return "";
        }
        if (LIST_LINE.matcher(line).find()) {
            return "\n";
        }
        return "\n\n";
    }

    public void updateDescriptionOfIssue(Issue issue, String text) {
        val currentDescription = issue.getDescription();
        val currentValues = currentDescription.split("\\n");
        if (this.hasSameValue(currentValues, text)) {
            logger.info("Current description has already same text: {}", text);
            return;
        }
        Optional<String> lastLine = currentValues.length == 0
                ? Optional.empty()
                : Optional.ofNullable(currentValues[currentValues.length - 1]);
        val description = currentDescription + this.getLineBreak(lastLine) + text;
        val params = new UpdateIssueParams(issue.getId())
                .description(description);
        client.updateIssue(params);
    }

    public void updateCustomFieldOfIssue(Issue issue, String fieldName, String addValue) {
        val opt = this.getCustomFieldOfIssue(issue, fieldName);
        if (opt.isPresent()) {
            val field = opt.get();
            if (field instanceof TextAreaCustomField) {
                val currentValue = ((TextAreaCustomField) field).getValue();
                if (this.hasSameValue(currentValue, addValue)) {
                    logger.info("Current value has already same text: {}", addValue);
                    return;
                }
                val value = currentValue + "\n" + addValue;
                val customFiledValue = new CustomFiledValue(field.getId(), value);
                val params = new UpdateIssueParams(issue.getId())
                        .customFieldOtherValue(customFiledValue);
                client.updateIssue(params);
                return;
            }
            logger.error("Supported custom field is TextAreaCustomField only");
        }
    }

    @SneakyThrows
    public static BacklogClient createClient(Config config) {
        val fqdn = config.getString(BacklogConfigKey.FQDN.get());
        val apiKey = config.getString(BacklogConfigKey.API_KEY.get());
        val backlogConfigure = BacklogDomain.create(fqdn, apiKey);
        return new BacklogClientFactory(backlogConfigure).newClient();
    }
}
