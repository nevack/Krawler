# Krawler

`Krawler` is a Kotlin CLI and library for checking Maven dependency updates from `group:artifact:version` input.

It resolves `maven-metadata.xml` directly with Ktor, supports repository-specific group routing, private repository auth, multiple output targets, and parallel dependency resolution.

## Features

- Kotlin multi-module project with `core` and `cli`
- CLI argument parsing with Clikt
- YAML config for repositories, auth, strategy, and outputs
- Dependency input from a separate file passed on the command line
- Built-in update strategies:
  - `latest`
  - `latest-snapshot`
  - `latest-minor`
  - `latest-patch`
- Custom `maven-metadata.xml` resolver with Ktor Client and OkHttp engine
- Output to multiple targets in one run, for example:
  - table to stdout
  - JSON to file
- Basic and bearer auth for private repositories
- Group filters per repository
- Interactive progress reporting when run in a terminal
- Parallel resolution with coroutines

## Build

Requirements used by the project:

- Java 21
- Gradle 9.4.1
- Kotlin 2.3.21

Build the project:

```bash
./gradlew test :cli:assemble
```

Fat jar output:

```text
cli/build/libs/cli-0.1.0-all.jar
```

## Usage

Run with a YAML config and a dependency input file:

```bash
java -jar cli/build/libs/cli-0.1.0-all.jar \
  --config samples/config.yml \
  --input samples/dependencies.txt
```

Show help:

```bash
java -jar cli/build/libs/cli-0.1.0-all.jar --help
```

## Dependency Input

The dependency list is a plain text file passed with `--input`.

Each line must be in GAV format:

```text
group:artifact:version
```

Example:

```text
androidx.annotation:annotation:1.10.0-rc02
androidx.core:core-ktx:1.18.0
androidx.activity:activity-ktx:1.13.0-beta03
androidx.fragment:fragment-ktx:1.8.9
```

Blank lines and `#` comments are ignored.

## Config

Example config:

```yaml
strategy: latest-minor

repositories:
  - id: google
    url: https://dl.google.com/dl/android/maven2
    includeGroups:
      - androidx.
      - com.google.android.
  - id: central
    url: https://repo1.maven.org/maven2

output:
  targets:
    - format: table
    - format: json
      file: reports/updates.json
```

### Strategy

Supported values:

- `latest`
- `latest-snapshot`
- `latest-minor`
- `latest-patch`

### Repositories

Each repository supports:

- `id`: logical repository name used in output
- `url`: base Maven repository URL
- `includeGroups`: optional list of group filters
- `auth`: optional auth block

If `includeGroups` is omitted, the repository is considered for all dependencies.

Group filter behavior:

- exact match: `com.example`
- prefix ending with `.`: `androidx.`
- wildcard suffix: `com.example.*`

### Auth

Supported auth types:

- `basic`
- `bearer`

Values can be supplied directly or via environment-variable reference.

Basic auth example:

```yaml
repositories:
  - id: private
    url: https://repo.example.com/maven/releases
    includeGroups:
      - com.mycompany.
    auth:
      basic:
        username:
          env: PRIVATE_REPO_USER
        password:
          env: PRIVATE_REPO_PASSWORD
```

Bearer auth example:

```yaml
repositories:
  - id: private
    url: https://repo.example.com/maven/releases
    auth:
      bearer:
        token: my-token
```

`ConfigValue` forms accepted by auth fields:

```yaml
token: direct-value
```

or

```yaml
token:
  env: MY_TOKEN
```

### Output

You can emit multiple outputs in one run.

Each target supports:

- `format`: `table` or `json`
- `file`: optional output path

If `file` is omitted, the target is written to stdout.

Example:

```yaml
output:
  targets:
    - format: table
    - format: json
      file: reports/updates.json
```

Legacy single-output shape is also supported and normalized internally:

```yaml
output:
  format: json
  file: reports/updates.json
```

## Interactive Progress

When run in an interactive terminal, the CLI writes progress messages to stderr while dependencies are being resolved.

Example messages:

```text
[1/12] Resolving androidx.core:core-ktx:1.18.0
[1/12] No update androidx.core:core-ktx
[2/12] Resolving androidx.activity:activity-ktx:1.13.0-beta03
[2/12] Update androidx.activity:activity-ktx 1.13.0-beta03 -> 1.13.0
```

Because resolution runs in parallel, progress completion order may differ from input order.

## Project Layout

- `core`: crawler logic, config loading, metadata resolution, version selection, and reporting
- `cli`: fat-jar CLI entrypoint
- `samples`: sample config and dependency input

## Current Coordinates

- Gradle group: `dev.nevack.krawler`
- Kotlin package prefix: `dev.nevack.krawler`
