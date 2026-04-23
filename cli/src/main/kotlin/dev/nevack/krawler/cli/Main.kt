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
    val configPath = parseConfigPath(args) ?: run {
        printUsage()
        exitProcess(1)
    }

    val client = HttpClient(OkHttp)
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
    println("Usage: java -jar krawler-all.jar --config path/to/config.yml")
}

private fun resolveAgainstConfig(configPath: Path, targetPath: String): Path =
    configPath.parent?.resolve(targetPath)?.normalize() ?: Path.of(targetPath)
