package dev.nevack.krawler.report

import dev.nevack.krawler.config.OutputFormat
import dev.nevack.krawler.config.OutputTarget
import dev.nevack.krawler.config.UpdateStrategy
import dev.nevack.krawler.model.AvailableUpdate
import dev.nevack.krawler.model.CrawlReport
import dev.nevack.krawler.model.Gav
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.time.Instant

class ReportOutputWriterTest {
    @Test
    fun `renders json using kotlinx serialization`() {
        val report = CrawlReport(
            strategy = UpdateStrategy.LATEST_PATCH,
            checkedDependencies = 1,
            updates = listOf(
                AvailableUpdate(
                    dependency = Gav("androidx.core", "core-ktx", "1.0.0"),
                    targetVersion = "1.0.1",
                    repositories = listOf("google"),
                ),
            ),
            generatedAt = Instant.parse("2026-04-23T00:00:00Z"),
        )

        val rendered = ReportOutputWriter().render(report, OutputFormat.JSON)

        assertTrue(rendered.contains("\"strategy\": \"LATEST_PATCH\""))
        assertTrue(rendered.contains("\"groupId\": \"androidx.core\""))
        assertTrue(rendered.contains("\"generatedAt\": \"2026-04-23T00:00:00Z\""))
    }

    @Test
    fun `writes report to stdout and file targets`() {
        val report = CrawlReport(
            strategy = UpdateStrategy.LATEST,
            checkedDependencies = 1,
            updates = listOf(
                AvailableUpdate(
                    dependency = Gav("androidx.core", "core-ktx", "1.0.0"),
                    targetVersion = "1.0.1",
                    repositories = listOf("google"),
                ),
            ),
            generatedAt = Instant.parse("2026-04-23T00:00:00Z"),
        )
        val outputDir = Files.createTempDirectory("krawler-output")
        val outputFile = outputDir.resolve("updates.json")
        val stdout = mutableListOf<String>()

        ReportOutputWriter().writeTargets(
            report = report,
            targets = listOf(
                OutputTarget(format = OutputFormat.TABLE),
                OutputTarget(format = OutputFormat.JSON, file = "updates.json"),
            ),
            resolvePath = outputDir::resolve,
            stdout = stdout::add,
        )

        assertEquals(1, stdout.size)
        assertTrue(stdout.single().contains("androidx.core:core-ktx"))
        assertTrue(Files.exists(outputFile))
        assertTrue(Files.readString(outputFile).contains("\"strategy\": \"LATEST\""))
    }
}
