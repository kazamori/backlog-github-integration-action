# backlog-github-integration-action

This action helps integrate with [Nulab's Backlog](https://nulab.com/products/backlog/) via GitHub events.

Here are the features.

* updates a Pull Request link into related issues when it has opened.
* updates commit links into related issues when it has pushed.

```mermaid
flowchart LR

subgraph gh [GitHub]
  direction TB
  repo(Repository) -- event --> gh-action(GitHub\nActions)
  gh-action -- checkout/pull --> bgia(Backlog-GitHub\nintegration action)
end

subgraph Internet
  bgia -- REST API --> backlog(Backlog)
end

user(Developer) -- work --> repo
user -- check the result --> backlog
```

## Usage

### Set Secrets for Backlog

For Backlog:

* secrets.BACKLOG_FQDN
* secrets.BACKLOG_API_KEY
* secrets.BACKLOG_PROJECT_KEY

For GitHub, you can use [secrets.GITHUB_TOKEN](https://docs.github.com/en/actions/security-guides/automatic-token-authentication) without configuring.

See the below example for detail.

### Create a workflow in your repository.

Here is an example.

```yml
name: Backlog integration

on:
  pull_request:
    types:
      - opened
  push:
    branches:
      - main

jobs:
  backlog-integration:
    runs-on: ubuntu-latest
    env:
      APP_LOCALE: "en_US"
      APP_LOG_LEVEL: "debug"
      BACKLOG_FQDN: ${{ secrets.BACKLOG_FQDN }}
      BACKLOG_API_KEY: ${{ secrets.BACKLOG_API_KEY }}
      BACKLOG_PROJECT_KEY: ${{ secrets.BACKLOG_PROJECT_KEY }}
      GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    steps:
      - name: integrate the pull request with backlog
        if: ${{ github.event_name == 'pull_request' && github.event.action == 'opened' }}
        uses: kazamori/backlog-github-integration-action@v1
        with:
          subcommand: "pull_request"
          args: "--repository ${{ github.repository }} --pr-number ${{ github.event.number }}"

      - name: integrate commits with backlog
        if: ${{ github.event_name == 'push' }}
        uses: kazamori/backlog-github-integration-action@v1
        with:
          subcommand: "push"
          args: "--repository ${{ github.repository }} --pusher ${{ github.event.pusher.name }} --commits '${{ toJson(github.event.commits) }}'"
```

By checking `${{ github.event_name }}`, some subcommands can be included in a single workflow yml. Issue IDs are taken from commit messages.

By default, the pull request link is added to the `Description` field. But if your Backlog has a custom field named `Pull Requests` (types: `Sentence`), set it like this.

```yml
        with:
          subcommand: "pull_request"
          args: "--repository ${{ github.repository }} --pr-number ${{ github.event.number }} --custom-field \"Pull Requests\""
```

### Actual example

You can see an actual example of the test repository to run workflows on GitHub actions.

* https://github.com/kazamori/test-gh-actions-repo/actions

#### Pull request

After these workflows run, a user of `BACKLOG_API_KEY` will write the `Description` field and the issue comments.

![](https://github.com/kazamori/backlog-github-integration-action/raw/main/example/pulls/figures/backlog-issue-comments1.png)

#### Push

After these workflows run, a user of `BACKLOG_API_KEY` will comment and change the issue status by commit messages.

![](https://github.com/kazamori/backlog-github-integration-action/raw/main/example/push/figures/backlog-issue-comments2.png)

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

#### Pull request

```bash
$ ./gradlew run --args="pull_request --repository kazamori/backlog-github-integration-action --pr-number 1"
```

#### Push

```bash
$ ./gradlew run --args="push --repository kazamori/backlog-github-integration-action --pusher t2y --commits '[{"key": "value", ...}]'"
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

## Release

This action is run as below sequence diagram.

```mermaid
sequenceDiagram

participant user as User
participant repo as Repository
participant action as GitHub Actions
participant bgia as Backlog-GitHub<br />integration action<br />repository
participant ghcr as GitHub Container Registry

user->>repo: work
repo->>action: event
action->>bgia: checkout [ ${REVISION} / ${TAG} / ${BRANCH} ]
bgia->>action: action.yml
Note over action,bgia: action.yml has docker URI to pull
action->>ghcr: docker pull ghcr.io/kazamori/backlog-github-integration-action:${TAG}
ghcr->>action: docker image
action->>action: docker run
```

Before releasing this action, we must understand two versions to control the workflow.

* action.yml version
* docker image version

First, the action.yml is controlled in this repository. We can checkout any action.yml by specifying REVISION/TAG/BRANCH. Next, the action.yml has docker URI indicates which version pulls from the container repository. For example, the following configuration uses the version added by `v1` tag.

```yml
runs:
  using: 'docker'
  image: 'docker://ghcr.io/kazamori/backlog-github-integration-action:v1'
```

Second, the docker image added by `v1` tag would be pulled and then run.

### GitHub Marketplace

* https://github.com/marketplace/actions/backlog-github-integration-action
