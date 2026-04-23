package dev.nevack.krawler.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class MavenKrawlerConfigLoaderTest {
    @Test
    fun `loads legacy single-output config with env-backed auth`() {
        val configFile = Files.createTempFile("krawler", ".yml")
        Files.writeString(
            configFile,
            """
            strategy: latest-minor
            repositories:
              - id: google
                url: https://dl.google.com/dl/android/maven2
                includeGroups:
                  - androidx.
              - id: private
                url: https://repo.example.com/releases
                auth:
                  bearer:
                    token: direct-token
            output:
              format: json
              file: reports/updates.json
            """.trimIndent(),
        )

        val config = MavenKrawlerConfigLoader().load(configFile)

        assertEquals(UpdateStrategy.LATEST_MINOR, config.strategy)
        assertEquals(1, config.output.targets.size)
        assertEquals(OutputFormat.JSON, config.output.targets.single().format)
        assertEquals("reports/updates.json", config.output.targets.single().file)
        assertTrue(config.repositories.first().includeGroups.contains("androidx."))
        assertEquals(
            ResolvedRepositoryAuth.Bearer("direct-token"),
            config.repositories.last().auth?.toResolvedAuth(),
        )
    }

    @Test
    fun `loads multiple output targets`() {
        val configFile = Files.createTempFile("krawler", ".yml")
        Files.writeString(
            configFile,
            """
            repositories:
              - id: central
                url: https://repo1.maven.org/maven2
            output:
              targets:
                - format: table
                - format: json
                  file: reports/updates.json
            """.trimIndent(),
        )

        val config = MavenKrawlerConfigLoader().load(configFile)

        assertEquals(2, config.output.targets.size)
        assertEquals(OutputFormat.TABLE, config.output.targets[0].format)
        assertEquals(null, config.output.targets[0].file)
        assertEquals(OutputFormat.JSON, config.output.targets[1].format)
        assertEquals("reports/updates.json", config.output.targets[1].file)
    }
}
