package dev.nevack.mavenkrawler.report

import dev.nevack.mavenkrawler.model.CrawlReport

class TableReportRenderer {
    fun render(report: CrawlReport): String {
        if (report.updates.isEmpty()) {
            return "Checked ${report.checkedDependencies} dependencies. No updates found."
        }

        val rows = report.updates.map {
            listOf(
                it.dependency.ga,
                it.dependency.version,
                it.targetVersion,
                it.repositories.joinToString(","),
            )
        }
        val headers = listOf("Dependency", "Current", "Target", "Repositories")
        val widths = headers.indices.map { index ->
            maxOf(headers[index].length, rows.maxOf { row -> row[index].length })
        }

        return buildString {
            appendLine("Checked ${report.checkedDependencies} dependencies. Found ${report.updates.size} updates.")
            appendLine(formatRow(headers, widths))
            appendLine(widths.joinToString("-+-") { "-".repeat(it) })
            rows.forEach { appendLine(formatRow(it, widths)) }
        }.trimEnd()
    }

    private fun formatRow(columns: List<String>, widths: List<Int>): String =
        columns.mapIndexed { index, value -> value.padEnd(widths[index]) }.joinToString(" | ")
}
