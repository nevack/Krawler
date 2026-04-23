package dev.nevack.krawler.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import dev.nevack.krawler.config.MavenKrawlerConfigLoader
import dev.nevack.krawler.input.DependencyInputReader
import dev.nevack.krawler.maven.KtorMavenMetadataSource
import dev.nevack.krawler.report.ReportOutputWriter
import dev.nevack.krawler.service.DependencyCrawler
import dev.nevack.krawler.version.VersionStrategySelector
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

fun main(args: Array<String>) = KrawlerCommand().main(args)

private class KrawlerCommand : CliktCommand(name = "krawler") {
    private val configPath by option("--config", help = "Path to YAML config file")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .required()

    private val inputPath by option("--input", help = "Path to dependency input file")
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .required()

    override fun run() {
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
                val report = crawler.crawl(config, inputPath)
                val writer = ReportOutputWriter()
                writer.writeTargets(report, config.output.targets, resolvePath = {
                    resolveAgainstConfig(configPath, it)
                }, stdout = ::echo)
            }
        } catch (exception: Exception) {
            echo(exception.message ?: exception::class.simpleName.orEmpty(), err = true)
            throw ProgramResult(1)
        } finally {
            client.close()
        }
    }
}

private fun resolveAgainstConfig(configPath: Path, targetPath: String): Path =
    configPath.parent?.resolve(targetPath)?.normalize() ?: Path.of(targetPath)
