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

class DependencyCrawler(
    private val inputReader: DependencyInputReader,
    private val metadataSource: MavenMetadataSource,
    private val versionStrategySelector: VersionStrategySelector,
) {
    suspend fun crawl(config: MavenKrawlerConfig, inputFile: Path): CrawlReport {
        val dependencies = inputReader.read(inputFile)
        val router = RepositoryRouter(config.repositories)

        val updates = dependencies.mapNotNull { dependency ->
            findUpdate(dependency, router, config.strategy)
        }

        return CrawlReport(
            strategy = config.strategy,
            checkedDependencies = dependencies.size,
            updates = updates,
        )
    }

    private suspend fun findUpdate(
        dependency: Gav,
        router: RepositoryRouter,
        strategy: dev.nevack.krawler.config.UpdateStrategy,
    ): AvailableUpdate? {
        val versionsByRepository = linkedMapOf<String, LinkedHashSet<String>>()

        for (repository in router.repositoriesFor(dependency.groupId)) {
            val metadata = metadataSource.fetch(repository, dependency) ?: continue
            val versions = metadata.allVersions()
            if (versions.isNotEmpty()) {
                versionsByRepository.getOrPut(repository.id) { linkedSetOf() }.addAll(versions)
            }
        }

        if (versionsByRepository.isEmpty()) {
            return null
        }

        val versions = versionsByRepository.values.flatten().distinct()
        val selectedVersion = versionStrategySelector.select(dependency.version, versions, strategy) ?: return null
        val repositories = versionsByRepository
            .filterValues { selectedVersion in it }
            .keys
            .toList()

        return AvailableUpdate(
            dependency = dependency,
            targetVersion = selectedVersion,
            repositories = repositories,
        )
    }
}
