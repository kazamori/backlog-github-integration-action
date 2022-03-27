# backlog-github-integration-action

This action helps integrate with [Nulab's backlog](https://nulab.com/products/backlog/) via GitHub events.

Here are the features.

* updates a Pull Request link into related issues when it has opened.

## Precondition to run

Set environment variables.

```bash
export APP_LOCALE="en_US"
export APP_LOG_LEVEL="debug"
export BACKLOG_FQDN="YOUR-SPACE.backlog.com"
export BACKLOG_API_KEY="..."
export BACKLOG_PROJECT_KEY="TEST"
export GITHUB_TOKEN="..."
```

## How to run

```bash
$ ./gradlew run --args="pull_request --repository kazamori/backlog-github-integration-action --pr-number 1"
```

## for debugging

### GitHub Client

```bash
$ ./gradlew githubClient --args='--repository kazamori/backlog-github-integration-action --pr-number 1'
```

### Backlog Client

```bash
$ ./gradlew backlogClient --args="--issue-id PROJECT-1"
```

```bash
$ ./gradlew backlogClient --args='--issue-id PROJECT-3 --custom-field "MyTextField" --issue-comment "* comment from REST API"'
```

## How to test

```bash
$ ./gradlew test --info
```
