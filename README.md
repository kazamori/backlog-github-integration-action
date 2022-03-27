# backlog-github-integration-action

This action helps integrate with [Nulab's backlog](https://nulab.com/products/backlog/) via GitHub events.

Here are the features.

* updates a Pull Request link into related issues when it has opened.

## Develop

### Precondition to run

Set environment variables.

```bash
export APP_LOCALE="en_US"
export APP_LOG_LEVEL="debug"
export BACKLOG_FQDN="YOUR-SPACE.backlog.com"
export BACKLOG_API_KEY="..."
export BACKLOG_PROJECT_KEY="TEST"
export GITHUB_TOKEN="..."
```

### How to run

```bash
$ ./gradlew run --args="pull_request --repository kazamori/backlog-github-integration-action --pr-number 1"
```

### for debugging

#### GitHub Client

```bash
$ ./gradlew githubClient --args='--repository kazamori/backlog-github-integration-action --pr-number 1'
```

#### Backlog Client

```bash
$ ./gradlew backlogClient --args="--issue-id PROJECT-1"
```

```bash
$ ./gradlew backlogClient --args='--issue-id PROJECT-3 --custom-field "MyTextField" --issue-comment "* comment from REST API"'
```

### How to test

```bash
$ ./gradlew test --info
```

## Deployment

### How to build a doker image

To build to a Docker daemon, like this.

```bash
$ echo $DOCKER_HUB_PASSWORD | docker login -u $DOCKER_HUB_USERNAME --password-stdin
$ ./gradlew jibDockerBuild
```

Confirm the target image was built.

```bash
$ docker images | grep backlog-github-integration-action
ghcr.io/kazamori/backlog-github-integration-action  latest  e25ab30a1bc0  52 years ago 154MB
```

Confirm the target image can be run.

```bash
$ docker run \
    --rm \
    --env APP_LOCALE=${APP_LOCALE} \
    --env APP_LOG_LEVEL=${APP_LOG_LEVEL} \
    --env BACKLOG_API_KEY=${BACKLOG_API_KEY} \
    --env BACKLOG_FQDN=${BACKLOG_FQDN} \
    --env BACKLOG_PROJECT_KEY=${BACKLOG_PROJECT_KEY} \
    --env GITHUB_TOKEN=${GITHUB_TOKEN} \
    ghcr.io/kazamori/backlog-github-integration-action \
        pull_request --repository kazamori/backlog-github-integration-action --pr-number 1
```

### How to publish

To publish to a Github Packages, like this.

```bash
$ echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin
$ ./gradlew clean jar
$ ./gradlew jib -Djib.container.labels="$(git rev-parse HEAD)"
```

Confirm a docker image has the git revision label.

```bash
$ docker pull ghcr.io/kazamori/backlog-github-integration-action:latest
$ docker inspect ghcr.io/kazamori/backlog-github-integration-action:latest | jq '.[].Config.Labels'
```
