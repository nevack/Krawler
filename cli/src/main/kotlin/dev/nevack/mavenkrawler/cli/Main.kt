package dev.nevack.mavenkrawler.cli

import dev.nevack.mavenkrawler.config.MavenKrawlerConfigLoader
import dev.nevack.mavenkrawler.maven.KtorMavenMetadataSource
import dev.nevack.mavenkrawler.report.ReportOutputWriter
import dev.nevack.mavenkrawler.service.DependencyCrawler
import dev.nevack.mavenkrawler.input.DependencyInputReader
import dev.nevack.mavenkrawler.version.VersionStrategySelector
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val configPath = parseConfigPath(args) ?: run {
        printUsage()
        exitProcess(1)
    }

    val client = HttpClient(CIO)
    try {
        runBlocking {
            val loader = MavenKrawlerConfigLoader()
            val config = loader.load(configPath)
            val crawler = DependencyCrawler(
                inputReader = DependencyInputReader(),
                metadataSource = KtorMavenMetadataSource(client),
                versionStrategySelector = VersionStrategySelector(),
            )
            val report = crawler.crawl(config, configPath)
            val writer = ReportOutputWriter()
            val rendered = writer.render(report, config.output.format)
            val outputPath = config.output.file?.let { resolveAgainstConfig(configPath, it) }
            writer.write(rendered, outputPath)
        }
    } catch (exception: Exception) {
        System.err.println(exception.message ?: exception::class.simpleName.orEmpty())
        exitProcess(1)
    } finally {
        client.close()
    }
}

private fun parseConfigPath(args: Array<String>): Path? {
    if (args.size == 1 && args[0] == "--help") {
        printUsage()
        exitProcess(0)
    }

    return when {
        args.size == 2 && args[0] == "--config" -> Path.of(args[1])
        args.size == 1 -> Path.of(args[0])
        else -> null
    }
}

private fun printUsage() {
    println("Usage: java -jar mavenkrawler-all.jar --config path/to/config.yml")
}

private fun resolveAgainstConfig(configPath: Path, targetPath: String): Path =
    configPath.parent?.resolve(targetPath)?.normalize() ?: Path.of(targetPath)
