package dev.nevack.krawler.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class MavenKrawlerConfigLoaderTest {
    @Test
    fun `loads yaml config with env-backed auth`() {
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
        assertEquals(OutputFormat.JSON, config.output.format)
        assertEquals("reports/updates.json", config.output.file)
        assertTrue(config.repositories.first().includeGroups.contains("androidx."))
        assertEquals(
            ResolvedRepositoryAuth.Bearer("direct-token"),
            config.repositories.last().auth?.toResolvedAuth(),
        )
    }
}
