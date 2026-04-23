package dev.nevack.krawler.report

import dev.nevack.krawler.config.UpdateStrategy
import dev.nevack.krawler.model.AvailableUpdate
import dev.nevack.krawler.model.CrawlReport
import dev.nevack.krawler.model.Gav
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TableReportRendererTest {
    @Test
    fun `renders update table`() {
        val report = CrawlReport(
            strategy = UpdateStrategy.LATEST,
            checkedDependencies = 2,
            updates = listOf(
                AvailableUpdate(
                    dependency = Gav("androidx.core", "core-ktx", "1.0.0"),
                    targetVersion = "1.1.0",
                    repositories = listOf("google"),
                ),
            ),
        )

        val rendered = TableReportRenderer().render(report)

        assertTrue(rendered.contains("androidx.core:core-ktx"))
        assertTrue(rendered.contains("1.1.0"))
        assertTrue(rendered.contains("google"))
    }
}
