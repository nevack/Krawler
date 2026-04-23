package dev.nevack.krawler.report

import dev.nevack.krawler.config.OutputFormat
import dev.nevack.krawler.model.CrawlReport
import java.nio.file.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class ReportOutputWriter(
    private val tableReportRenderer: TableReportRenderer = TableReportRenderer(),
) {
    private val json = Json {
        prettyPrint = true
    }

    fun render(report: CrawlReport, format: OutputFormat): String = when (format) {
        OutputFormat.TABLE -> tableReportRenderer.render(report)
        OutputFormat.JSON -> json.encodeToString(report)
    }

    fun write(rendered: String, outputFile: Path?) {
        if (outputFile == null) {
            println(rendered)
            return
        }

        outputFile.parent?.createDirectories()
        outputFile.writeText(rendered)
    }
}
