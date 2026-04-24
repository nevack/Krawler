# MavenKrawler Agent Notes

## Build And Verify
- Use the wrapper: `./gradlew`.
- Full repo verification from `README.md`: `./gradlew test :cli:assemble`
- Fast library-only verification: `./gradlew :core:test`
- Single test: `./gradlew :core:test --tests 'dev.nevack.krawler.service.DependencyCrawlerTest'`
- Run the CLI without packaging first: `./gradlew :cli:run --args="--config samples/config.yml --input samples/dependencies.txt"`
- Build the executable jar at `dist/krawler.jar`: `./gradlew :cli:shadowJar` or `./gradlew :cli:assemble`

## Project Shape
- Gradle multi-project build with included build logic in `build-logic/`; shared Kotlin/JVM conventions live there.
- `core/` contains the real behavior: config loading, repository routing, Maven metadata fetching, version selection, and report writing.
- `cli/` is a thin Clikt entrypoint in `cli/src/main/kotlin/dev/nevack/krawler/cli/Main.kt` that wires `core` together.

## Behavior That Is Easy To Miss
- Config parsing is strict: `MavenKrawlerConfigLoader` enables `FAIL_ON_UNKNOWN_PROPERTIES`, so extra YAML keys fail loading.
- `strategy` defaults to `latest` when omitted.
- `output` defaults to a single table target when omitted, and the legacy single-output shape (`output.format` / `output.file`) is still accepted.
- Output file paths are resolved relative to the config file directory, not the current working directory; parent directories are created automatically.
- Repository routing is additive, not first-match: repositories with empty `includeGroups` are always queried, plus every filtered repo whose pattern matches, in config order.
- `latest`, `latest-minor`, and `latest-patch` reject snapshots and reject prereleases unless the current version is already a prerelease. Only `latest-snapshot` allows all newer versions.
- Dependency resolution is intentionally parallel in `DependencyCrawler`; preserve concurrency behavior when refactoring.
- CLI progress messages only appear in interactive sessions (`System.console() != null`) and are written to stderr, so do not expect them in non-interactive runs or tests.

## Testing Notes
- HTTP behavior is unit-tested in `:core` with Ktor `MockEngine`; prefer adding coverage there instead of relying on live repositories.
