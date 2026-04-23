package dev.nevack.krawler.repo

import dev.nevack.krawler.config.RepositoryConfig

class RepositoryRouter(
    private val repositories: List<RepositoryConfig>,
) {
    fun repositoriesFor(groupId: String): List<RepositoryConfig> = repositories.filter { repository ->
        repository.includeGroups.isEmpty() || repository.includeGroups.any { GroupPatternMatcher.matches(groupId, it) }
    }
}
