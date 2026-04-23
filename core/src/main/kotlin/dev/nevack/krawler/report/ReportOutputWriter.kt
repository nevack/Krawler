package dev.nevack.krawler.report

import dev.nevack.krawler.config.OutputFormat
import dev.nevack.krawler.config.OutputTarget
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

    fun writeTargets(
        report: CrawlReport,
        targets: List<OutputTarget>,
        resolvePath: (String) -> Path,
        stdout: (String) -> Unit = ::println,
    ) {
        targets.forEach { target ->
            write(render(report, target.format), target.file?.let(resolvePath), stdout)
        }
    }

    private fun write(rendered: String, outputFile: Path?, stdout: (String) -> Unit) {
        if (outputFile == null) {
            stdout(rendered)
            return
        }

        outputFile.parent?.createDirectories()
        outputFile.writeText(rendered)
    }
}
