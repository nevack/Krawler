package dev.nevack.krawler.maven

import dev.nevack.krawler.config.BasicAuthConfig
import dev.nevack.krawler.config.ConfigValue
import dev.nevack.krawler.config.RepositoryAuthConfig
import dev.nevack.krawler.config.RepositoryConfig
import dev.nevack.krawler.model.Gav
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KtorMavenMetadataSourceTest {
    @Test
    fun `requests maven metadata and applies basic auth header`() = runBlocking {
        var authorizationHeader: String? = null
        val engine = MockEngine { request ->
            authorizationHeader = request.headers[HttpHeaders.Authorization]
            respond(
                content = """
                    <metadata>
                      <versioning>
                        <versions>
                          <version>1.0.0</version>
                          <version>1.1.0</version>
                        </versions>
                      </versioning>
                    </metadata>
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val client = HttpClient(engine)
        val source = KtorMavenMetadataSource(client)

        val metadata = source.fetch(
            RepositoryConfig(
                id = "private",
                url = "https://repo.example.com/maven",
                auth = RepositoryAuthConfig(
                    basic = BasicAuthConfig(
                        username = ConfigValue(value = "user"),
                        password = ConfigValue(value = "secret"),
                    ),
                ),
            ),
            Gav("com.acme", "demo", "1.0.0"),
        )

        assertTrue(authorizationHeader!!.startsWith("Basic "))
        assertEquals(listOf("1.0.0", "1.1.0"), metadata?.versions)
        client.close()
    }
}
