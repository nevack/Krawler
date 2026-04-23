package dev.nevack.krawler.repo

import dev.nevack.krawler.config.RepositoryConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RepositoryRouterTest {
    @Test
    fun `returns matching and unfiltered repositories in config order`() {
        val router = RepositoryRouter(
            listOf(
                RepositoryConfig(id = "central", url = "https://repo1.maven.org/maven2"),
                RepositoryConfig(id = "google", url = "https://dl.google.com/dl/android/maven2", includeGroups = listOf("androidx.")),
                RepositoryConfig(id = "private", url = "https://repo.example.com", includeGroups = listOf("com.acme.*")),
            ),
        )

        val repositories = router.repositoriesFor("androidx.core")

        assertEquals(listOf("central", "google"), repositories.map { it.id })
    }
}
