package dev.nevack.krawler.service

import dev.nevack.krawler.config.MavenKrawlerConfig
import dev.nevack.krawler.config.RepositoryConfig
import dev.nevack.krawler.config.UpdateStrategy
import dev.nevack.krawler.input.DependencyInputReader
import dev.nevack.krawler.maven.MavenMetadata
import dev.nevack.krawler.maven.MavenMetadataSource
import dev.nevack.krawler.model.AvailableUpdate
import dev.nevack.krawler.model.Gav
import dev.nevack.krawler.version.VersionStrategySelector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

class DependencyCrawlerTest {
    @Test
    fun `aggregates versions across matching repositories`() = runBlocking {
        val configFile = Files.createTempFile("krawler", ".yml")
        val inputFile = configFile.parent.resolve("deps.txt")
        Files.writeString(inputFile, "androidx.core:core-ktx:1.0.0")

        val crawler = DependencyCrawler(
            inputReader = DependencyInputReader(),
            metadataSource = object : MavenMetadataSource {
                override suspend fun fetch(repository: RepositoryConfig, dependency: Gav): MavenMetadata? = when (repository.id) {
                    "google" -> MavenMetadata(versions = listOf("1.0.1"))
                    "mirror" -> MavenMetadata(versions = listOf("1.1.0"))
                    else -> null
                }
            },
            versionStrategySelector = VersionStrategySelector(),
        )

        val report = crawler.crawl(
            MavenKrawlerConfig(
                strategy = UpdateStrategy.LATEST,
                repositories = listOf(
                    RepositoryConfig(id = "google", url = "https://google.example", includeGroups = listOf("androidx.")),
                    RepositoryConfig(id = "mirror", url = "https://mirror.example"),
                ),
            ),
            inputFile = inputFile,
        )

        assertEquals(1, report.updates.size)
        assertEquals("1.1.0", report.updates.single().targetVersion)
        assertEquals(listOf("mirror"), report.updates.single().repositories)
    }

    @Test
    fun `emits progress events for dependency resolution`() = runBlocking {
        val configFile = Files.createTempFile("krawler", ".yml")
        val inputFile = configFile.parent.resolve("deps.txt")
        Files.writeString(inputFile, "androidx.core:core-ktx:1.0.0")
        val events = mutableListOf<CrawlProgressEvent>()

        val crawler = DependencyCrawler(
            inputReader = DependencyInputReader(),
            metadataSource = object : MavenMetadataSource {
                override suspend fun fetch(repository: RepositoryConfig, dependency: Gav): MavenMetadata? =
                    MavenMetadata(versions = listOf("1.0.1"))
            },
            versionStrategySelector = VersionStrategySelector(),
        )

        crawler.crawl(
            MavenKrawlerConfig(
                strategy = UpdateStrategy.LATEST,
                repositories = listOf(
                    RepositoryConfig(id = "google", url = "https://google.example", includeGroups = listOf("androidx.")),
                ),
            ),
            inputFile = inputFile,
            progressListener = CrawlProgressListener(events::add),
        )

        assertEquals(2, events.size)
        assertTrue(events[0] is CrawlProgressEvent.DependencyStarted)
        assertTrue(events[1] is CrawlProgressEvent.DependencyFinished)
        val finishedEvent = events[1] as CrawlProgressEvent.DependencyFinished
        assertEquals("androidx.core:core-ktx", finishedEvent.dependency.ga)
        assertEquals("1.0.1", finishedEvent.update?.targetVersion)
    }
}
