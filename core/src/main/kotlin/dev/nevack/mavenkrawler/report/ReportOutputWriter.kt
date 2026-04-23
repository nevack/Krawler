package dev.nevack.mavenkrawler.report

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.nevack.mavenkrawler.config.OutputFormat
import dev.nevack.mavenkrawler.model.CrawlReport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class ReportOutputWriter(
    private val tableReportRenderer: TableReportRenderer = TableReportRenderer(),
) {
    private val jsonMapper = jacksonObjectMapper()

    fun render(report: CrawlReport, format: OutputFormat): String = when (format) {
        OutputFormat.TABLE -> tableReportRenderer.render(report)
        OutputFormat.JSON -> jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report)
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
