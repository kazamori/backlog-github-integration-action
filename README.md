# backlog-github-integration-action

This action helps integrate with [Nulab's Backlog](https://nulab.com/products/backlog/) via GitHub events.

Here are the features.

* updates a Pull Request link into related issues when it has opened.

## Usage

1. Set Secrets for Backlog

For Backlog:

* secrets.BACKLOG_FQDN
* secrets.BACKLOG_API_KEY
* secrets.BACKLOG_PROJECT_KEY

For GitHub, you can use [secrets.GITHUB_TOKEN](https://docs.github.com/en/actions/security-guides/automatic-token-authentication) without configuring.

See the below example for detail.

2. Create workflow for pull request in your repository.

Here is an example.

```yml
name: Pull request

on:
  pull_request:
    types:
      - opened

jobs:
  pr-integration:
    runs-on: ubuntu-latest
    steps:
      - name: integrate the pull request with backlog
        if: ${{ github.event_name == 'pull_request' && github.event.action == 'opened' }}
        uses: kazamori/backlog-github-integration-action@main
        with:
          subcommand: "pull_request"
          args: "--repository ${{ github.repository }} --pr-number ${{ github.event.number }}"
        env:
          APP_LOCALE: "en_US"
          APP_LOG_LEVEL: "debug"
          BACKLOG_FQDN: ${{ secrets.BACKLOG_FQDN }}
          BACKLOG_API_KEY: ${{ secrets.BACKLOG_API_KEY }}
          BACKLOG_PROJECT_KEY: ${{ secrets.BACKLOG_PROJECT_KEY }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

Currently, this action is intended to be used for an opened event when a pull request was created. Issue IDs are taken from commit messages in the pull request.

By default, the pull request link is added to the `Description` field. But if your Backlog has a custom field named `Pull Requests` (types: `Sentence`), set it like this.

```yml
        with:
          subcommand: "pull_request"
          args: "--repository ${{ github.repository }} --pr-number ${{ github.event.number }} --custom-field \"Pull Requests\""
```

### Actual example

You can see an actual example of the test repository to run workflows on GitHub actions.

* https://github.com/kazamori/test-gh-actions-repo/actions

After these workflows run, a user of `BACKLOG_API_KEY` will write the `Description` field and the issue comments.

![](https://github.com/kazamori/backlog-github-integration-action/raw/main/example/pulls/figures/backlog-issue-comments1.png)

## Develop

### Precondition to run

Set environment variables.

```bash
export APP_LOCALE="en_US"
export APP_LOG_LEVEL="debug"
export BACKLOG_FQDN="YOUR-SPACE.backlog.com"
export BACKLOG_API_KEY="..."
export BACKLOG_PROJECT_KEY="YOURPROJECT"
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
    --env APP_LOCALE \
    --env APP_LOG_LEVEL \
    --env BACKLOG_API_KEY \
    --env BACKLOG_FQDN \
    --env BACKLOG_PROJECT_KEY \
    --env GITHUB_TOKEN \
    ghcr.io/kazamori/backlog-github-integration-action \
        pull_request --repository kazamori/backlog-github-integration-action --pr-number 1
```

### How to publish

To publish to a Github Packages, like this.

```bash
$ echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin
$ ./gradlew clean jar
$ ./gradlew jib -Djib.container.labels="version.backlog-github-integration-action=$(git rev-parse HEAD)"
```

Confirm a docker image has the git revision label.

```bash
$ docker pull ghcr.io/kazamori/backlog-github-integration-action:latest
$ docker inspect ghcr.io/kazamori/backlog-github-integration-action:latest | jq '.[].Config.Labels'
{
  "version.backlog-github-integration-action": "4c97539929bcd3e9af1989fe03e6dbc9b3851d3e"
}
```
