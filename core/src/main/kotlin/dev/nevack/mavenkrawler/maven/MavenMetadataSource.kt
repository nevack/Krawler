package dev.nevack.mavenkrawler.maven

import dev.nevack.mavenkrawler.config.RepositoryConfig
import dev.nevack.mavenkrawler.model.Gav

interface MavenMetadataSource {
    suspend fun fetch(repository: RepositoryConfig, dependency: Gav): MavenMetadata?
}
