package dev.nevack.krawler.config

import kotlinx.serialization.Serializable

data class MavenKrawlerConfig(
    val strategy: UpdateStrategy = UpdateStrategy.LATEST,
    val repositories: List<RepositoryConfig>,
    val output: OutputConfig = OutputConfig(),
)

@Serializable
enum class UpdateStrategy {
    LATEST,
    LATEST_SNAPSHOT,
    LATEST_MINOR,
    LATEST_PATCH,
    ;

    companion object {
        fun fromConfigValue(value: String): UpdateStrategy = when (value.trim().lowercase()) {
            "latest" -> LATEST
            "latest-snapshot" -> LATEST_SNAPSHOT
            "latest-minor" -> LATEST_MINOR
            "latest-patch" -> LATEST_PATCH
            else -> error("Unsupported update strategy '$value'")
        }
    }
}

data class RepositoryConfig(
    val id: String,
    val url: String,
    val includeGroups: List<String> = emptyList(),
    val auth: RepositoryAuthConfig? = null,
)

data class RepositoryAuthConfig(
    val basic: BasicAuthConfig? = null,
    val bearer: BearerAuthConfig? = null,
) {
    fun toResolvedAuth(): ResolvedRepositoryAuth? {
        val configuredTypes = listOfNotNull(basic, bearer)
        require(configuredTypes.size <= 1) { "Repository auth must define only one auth type" }

        return when {
            basic != null -> ResolvedRepositoryAuth.Basic(
                username = basic.username.resolve(),
                password = basic.password.resolve(),
            )

            bearer != null -> ResolvedRepositoryAuth.Bearer(token = bearer.token.resolve())
            else -> null
        }
    }
}

data class BasicAuthConfig(
    val username: ConfigValue,
    val password: ConfigValue,
)

data class BearerAuthConfig(
    val token: ConfigValue,
)

data class OutputConfig(
    val format: OutputFormat = OutputFormat.TABLE,
    val file: String? = null,
)

enum class OutputFormat {
    TABLE,
    JSON,
    ;

    companion object {
        fun fromConfigValue(value: String): OutputFormat = when (value.trim().lowercase()) {
            "table" -> TABLE
            "json" -> JSON
            else -> error("Unsupported output format '$value'")
        }
    }
}
