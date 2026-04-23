package dev.nevack.mavenkrawler.maven

import dev.nevack.mavenkrawler.config.RepositoryConfig
import dev.nevack.mavenkrawler.config.ResolvedRepositoryAuth
import dev.nevack.mavenkrawler.model.Gav
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.util.Base64

class KtorMavenMetadataSource(
    private val client: HttpClient,
    private val parser: MavenMetadataParser = MavenMetadataParser(),
) : MavenMetadataSource {
    override suspend fun fetch(repository: RepositoryConfig, dependency: Gav): MavenMetadata? {
        val response = client.get(metadataUrl(repository, dependency)) {
            applyAuthentication(repository.toResolvedAuth())
        }

        return when (response.status) {
            HttpStatusCode.NotFound -> null
            HttpStatusCode.OK -> parser.parse(response.body<String>())
            else -> error(
                "Failed to resolve ${dependency.ga} from repository '${repository.id}': ${response.status.value} ${response.status.description}",
            )
        }
    }

    internal fun metadataUrl(repository: RepositoryConfig, dependency: Gav): String {
        val groupPath = dependency.groupId.replace('.', '/')
        return "${repository.url.trimEnd('/')}/$groupPath/${dependency.artifactId}/maven-metadata.xml"
    }

    private fun io.ktor.client.request.HttpRequestBuilder.applyAuthentication(auth: ResolvedRepositoryAuth?) {
        when (auth) {
            is ResolvedRepositoryAuth.Basic -> {
                val token = Base64.getEncoder().encodeToString("${auth.username}:${auth.password}".toByteArray())
                header(HttpHeaders.Authorization, "Basic $token")
            }

            is ResolvedRepositoryAuth.Bearer -> header(HttpHeaders.Authorization, "Bearer ${auth.token}")
            null -> Unit
        }
    }

    private fun RepositoryConfig.toResolvedAuth(): ResolvedRepositoryAuth? = auth?.toResolvedAuth()
}
