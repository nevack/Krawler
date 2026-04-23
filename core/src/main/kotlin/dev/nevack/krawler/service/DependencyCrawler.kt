package dev.nevack.krawler.service

import dev.nevack.krawler.config.MavenKrawlerConfig
import dev.nevack.krawler.input.DependencyInputReader
import dev.nevack.krawler.maven.MavenMetadataSource
import dev.nevack.krawler.model.AvailableUpdate
import dev.nevack.krawler.model.CrawlReport
import dev.nevack.krawler.model.Gav
import dev.nevack.krawler.repo.RepositoryRouter
import dev.nevack.krawler.version.VersionStrategySelector
import java.nio.file.Path
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DependencyCrawler(
    private val inputReader: DependencyInputReader,
    private val metadataSource: MavenMetadataSource,
    private val versionStrategySelector: VersionStrategySelector,
) {
    suspend fun crawl(
        config: MavenKrawlerConfig,
        inputFile: Path,
        progressListener: CrawlProgressListener? = null,
    ): CrawlReport = coroutineScope {
        val dependencies = inputReader.read(inputFile)
        val router = RepositoryRouter(config.repositories)
        val progressLock = Mutex()

        val updates = dependencies.mapIndexed { index, dependency ->
            async {
                val sequence = index + 1
                emitProgress(progressListener, progressLock) {
                    CrawlProgressEvent.DependencyStarted(sequence, dependencies.size, dependency)
                }
                val update = findUpdate(dependency, router, config.strategy)
                emitProgress(progressListener, progressLock) {
                    CrawlProgressEvent.DependencyFinished(sequence, dependencies.size, dependency, update)
                }
                update
            }
        }.awaitAll().filterNotNull()

        CrawlReport(
            strategy = config.strategy,
            checkedDependencies = dependencies.size,
            updates = updates,
        )
    }

    private suspend fun findUpdate(
        dependency: Gav,
        router: RepositoryRouter,
        strategy: dev.nevack.krawler.config.UpdateStrategy,
    ): AvailableUpdate? = coroutineScope {
        val versionsByRepository = router.repositoriesFor(dependency.groupId)
            .map { repository ->
                async {
                    val metadata = metadataSource.fetch(repository, dependency) ?: return@async null
                    val versions = metadata.allVersions()
                    if (versions.isEmpty()) {
                        null
                    } else {
                        repository.id to LinkedHashSet(versions)
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
            .associateTo(linkedMapOf()) { it }

        if (versionsByRepository.isEmpty()) {
            return@coroutineScope null
        }

        val versions = versionsByRepository.values.flatten().distinct()
        val selectedVersion = versionStrategySelector.select(dependency.version, versions, strategy) ?: return@coroutineScope null
        val repositories = versionsByRepository
            .filterValues { selectedVersion in it }
            .keys
            .toList()

        AvailableUpdate(
            dependency = dependency,
            targetVersion = selectedVersion,
            repositories = repositories,
        )
    }

    private suspend fun emitProgress(
        progressListener: CrawlProgressListener?,
        progressLock: Mutex,
        event: () -> CrawlProgressEvent,
    ) {
        if (progressListener == null) {
            return
        }

        progressLock.withLock {
            progressListener.onEvent(event())
        }
    }
}

fun interface CrawlProgressListener {
    fun onEvent(event: CrawlProgressEvent)
}

sealed interface CrawlProgressEvent {
    data class DependencyStarted(
        val current: Int,
        val total: Int,
        val dependency: Gav,
    ) : CrawlProgressEvent

    data class DependencyFinished(
        val current: Int,
        val total: Int,
        val dependency: Gav,
        val update: AvailableUpdate?,
    ) : CrawlProgressEvent
}
