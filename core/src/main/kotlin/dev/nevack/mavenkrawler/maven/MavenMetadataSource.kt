package dev.nevack.krawler.maven

import dev.nevack.krawler.config.RepositoryConfig
import dev.nevack.krawler.model.Gav

interface MavenMetadataSource {
    suspend fun fetch(repository: RepositoryConfig, dependency: Gav): MavenMetadata?
}
