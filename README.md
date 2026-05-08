# Set Agent Invoker

Event collection tool that monitors GitHub pull requests and Jira issues, then processes each event through a configurable AI agent (Claude, IBM BOB, or any CLI-based model).

## Modules

| Module | Description |
|---|---|
| `agent-invoker-core` | Core interfaces: `ApplicationEvent`, `AgentService`, `EventCollector`, `ConfigStore`, `StateStore` |
| `agent-invoker-cli` | CLI entry point (Picocli + JLine3 REPL) |
| `collector-github` | GitHub pull-request collector via REST API |
| `collector-jira` | Jira issue collector via JQL search (`/rest/api/3/search/jql`) |
| `agent-claude` | Agent implementation wrapping the Claude Code CLI |
| `agent-ibm-bob` | Agent implementation wrapping the IBM BOB CLI |
| `state-filesystem` | Filesystem-backed config and state store |
| `state-github` | GitHub-backed config and state store (stores JSON in a repo file) |
| `marshaller-json` | Jackson-based JSON marshaller |

## Build

```bash
mvn clean package -pl agent-invoker-cli -am
```

The uber-jar is produced at:

```
agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar
```

## Startup options

| Option | Required | Description |
|---|---|---|
| `--store <name>` | Yes | Store implementation: `filesystem` or `github` |
| `--marshaller <name>` | Yes | Marshaller: `json` |
| `--store-uri <path\|url>` | Yes | Base folder for config and state files |
| `--agent <name>` | No | Default agent: `claude` (default) or `ibm-bob` |

Config is persisted as `<store-uri>/companion-config.json` and state as `<store-uri>/companion-state.json`.

## Full example — GitHub + Jira setup

### Step 1 — Set credentials as environment variables

```bash
export GITHUB_TOKEN=ghp_your_token_here
export JIRA_TOKEN=your_jira_api_token
export JIRA_EMAIL=you@example.com
```

### Step 2 — Add a GitHub pull-request collector

```bash
java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion \
  config add \
    --type github \
    --url https://api.github.com/repos/jboss-set/aphrodite/events \
    --token '${GITHUB_TOKEN}'
```

### Step 3 — Add a Jira issue collector with project filter

```bash
java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion \
  config add \
    --type jira \
    --url https://redhat.atlassian.net \
    --user '${JIRA_EMAIL}' \
    --token '${JIRA_TOKEN}'

java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion \
  config filter jira https://redhat.atlassian.net project=JBEAP
```

### Step 4 — Set the agent and prompt

```bash
java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion \
  config agent set \
    --impl claude \
    --prompt "Analyse the following event and summarise what action is required: [event]"
```

`[event]` is replaced at runtime with the string representation of each event.

### Step 5 — Collect and process

```bash
java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion \
  collect
```

Collect a specific source only:

```bash
java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion \
  collect jira https://redhat.atlassian.net
```

---

## Interactive (REPL) mode

Start without a subcommand to enter the interactive REPL:

```bash
java -jar agent-invoker-cli/target/agent-invoker-cli-1.0.0-SNAPSHOT.jar \
  --store filesystem --marshaller json --store-uri /opt/companion
```

```
invoker> config add --type github --url https://api.github.com/repos/jboss-set/aphrodite/events --token '${GITHUB_TOKEN}'
invoker> config add --type jira --url https://redhat.atlassian.net --user '${JIRA_EMAIL}' --token '${JIRA_TOKEN}'
invoker> config filter jira https://redhat.atlassian.net project=JBEAP
invoker> config agent set --impl claude --prompt "Analyse the following event and summarise what action is required: [event]"
invoker> config show
invoker> collect
invoker> exit
```

Tab completion is available for all commands and subcommands.

---

## Agent configuration

The agent is configured at the store level (shared across all sources) via `config agent set`:

| Option | Description |
|---|---|
| `--impl <name>` | Agent to use: `claude` or `ibm-bob` |
| `--prompt <text>` | Prompt sent to the agent for every event. Use `[event]` as a placeholder for the event details. |

View the current agent configuration:

```bash
config agent show
```

### Claude agent environment variables

| Variable | Default | Description |
|---|---|---|
| `CLAUDE_COMMAND` | `claude` | Path or name of the Claude CLI binary |
| `CLAUDE_MODEL` | _(unset)_ | Model override (e.g. `claude-opus-4-7`) |

### IBM BOB agent environment variables

| Variable | Default | Description |
|---|---|---|
| `IBM_BOB_COMMAND` | `ibmcloud` | Path or name of the ibmcloud CLI binary |
| `IBM_BOB_ARGS` | `ml,text-generation,--input` | Comma-separated CLI arguments |
| `IBM_BOB_STDIN` | `false` | Set to `true` to send the prompt via stdin instead of as a positional argument |

---

## State management

The checkpoint for each source is updated after every successful collection run. To reset a checkpoint (re-process from the last 24 hours):

```bash
state reset jira https://redhat.atlassian.net
```

To set an explicit checkpoint (ISO-8601 instant for Jira, ETag for GitHub):

```bash
state set-checkpoint jira https://redhat.atlassian.net 2025-01-01T00:00:00Z
```

View all checkpoints:

```bash
state show
```

---

## Jira collector notes

- Uses `/rest/api/3/search/jql` (Jira Cloud REST API v3)
- Collects issues updated since the last checkpoint, excluding issues in the **Done** status category (closed/resolved)
- Supported filter key: `project` (e.g. `project=JBEAP`)
- Fields collected per issue: `summary`, `status`, `priority`, `assignee`, `reporter`, `pm_ack`, `dev_ack`, `qe_ack`, `target_release`, `affected_version`
- Checkpoint is an ISO-8601 instant; defaults to 24 hours ago on first run
