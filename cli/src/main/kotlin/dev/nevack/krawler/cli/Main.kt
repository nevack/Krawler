package dev.nevack.krawler.cli

import dev.nevack.krawler.config.MavenKrawlerConfigLoader
import dev.nevack.krawler.maven.KtorMavenMetadataSource
import dev.nevack.krawler.report.ReportOutputWriter
import dev.nevack.krawler.service.DependencyCrawler
import dev.nevack.krawler.input.DependencyInputReader
import dev.nevack.krawler.version.VersionStrategySelector
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val cliArguments = parseArguments(args) ?: run {
        printUsage()
        exitProcess(1)
    }

    val client = HttpClient(OkHttp)
    try {
        runBlocking {
            val loader = MavenKrawlerConfigLoader()
            val config = loader.load(cliArguments.configPath)
            val crawler = DependencyCrawler(
                inputReader = DependencyInputReader(),
                metadataSource = KtorMavenMetadataSource(client),
                versionStrategySelector = VersionStrategySelector(),
            )
            val report = crawler.crawl(config, cliArguments.inputPath)
            val writer = ReportOutputWriter()
            val rendered = writer.render(report, config.output.format)
            val outputPath = config.output.file?.let { resolveAgainstConfig(cliArguments.configPath, it) }
            writer.write(rendered, outputPath)
        }
    } catch (exception: Exception) {
        System.err.println(exception.message ?: exception::class.simpleName.orEmpty())
        exitProcess(1)
    } finally {
        client.close()
    }
}

private data class CliArguments(
    val configPath: Path,
    val inputPath: Path,
)

private fun parseArguments(args: Array<String>): CliArguments? {
    if (args.size == 1 && args[0] == "--help") {
        printUsage()
        exitProcess(0)
    }

    if (args.size != 4) {
        return null
    }

    var configPath: Path? = null
    var inputPath: Path? = null

    for (index in args.indices step 2) {
        val option = args[index]
        val value = args.getOrNull(index + 1) ?: return null
        when (option) {
            "--config" -> configPath = Path.of(value)
            "--input" -> inputPath = Path.of(value)
            else -> return null
        }
    }

    return if (configPath != null && inputPath != null) {
        CliArguments(configPath = configPath, inputPath = inputPath)
    } else {
        null
    }
}

private fun printUsage() {
    println("Usage: java -jar krawler-all.jar --config path/to/config.yml --input path/to/dependencies.txt")
}

private fun resolveAgainstConfig(configPath: Path, targetPath: String): Path =
    configPath.parent?.resolve(targetPath)?.normalize() ?: Path.of(targetPath)
