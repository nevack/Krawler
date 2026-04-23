package dev.nevack.krawler.config

sealed interface ResolvedRepositoryAuth {
    data class Basic(
        val username: String,
        val password: String,
    ) : ResolvedRepositoryAuth

    data class Bearer(
        val token: String,
    ) : ResolvedRepositoryAuth
}
