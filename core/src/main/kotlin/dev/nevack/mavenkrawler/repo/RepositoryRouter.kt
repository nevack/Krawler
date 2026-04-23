package dev.nevack.mavenkrawler.repo

import dev.nevack.mavenkrawler.config.RepositoryConfig

class RepositoryRouter(
    private val repositories: List<RepositoryConfig>,
) {
    fun repositoriesFor(groupId: String): List<RepositoryConfig> = repositories.filter { repository ->
        repository.includeGroups.isEmpty() || repository.includeGroups.any { GroupPatternMatcher.matches(groupId, it) }
    }
}
