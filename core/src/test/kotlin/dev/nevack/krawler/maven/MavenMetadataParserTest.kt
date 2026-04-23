package dev.nevack.krawler.maven

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MavenMetadataParserTest {
    @Test
    fun `parses repository metadata with namespace`() {
        val metadata = MavenMetadataParser().parse(
            """
            <metadata xmlns="http://maven.apache.org/METADATA/1.1.0">
              <versioning>
                <latest>1.3.0-beta01</latest>
                <release>1.2.0</release>
                <versions>
                  <version>1.0.0</version>
                  <version>1.2.0</version>
                  <version>1.3.0-beta01</version>
                </versions>
              </versioning>
            </metadata>
            """.trimIndent(),
        )

        assertEquals(listOf("1.0.0", "1.2.0", "1.3.0-beta01"), metadata.versions)
        assertEquals("1.3.0-beta01", metadata.latest)
        assertEquals("1.2.0", metadata.release)
    }
}
