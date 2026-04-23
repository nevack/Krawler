package dev.nevack.krawler.input

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.file.Files

class DependencyInputReaderTest {
    @Test
    fun `reads dependencies and ignores comments`() {
        val input = Files.createTempFile("krawler", ".txt")
        Files.writeString(
            input,
            """
            # comment
            androidx.core:core-ktx:1.18.0

            androidx.fragment:fragment-ktx:1.8.9
            """.trimIndent(),
        )

        val dependencies = DependencyInputReader().read(input)

        assertEquals(2, dependencies.size)
        assertEquals("androidx.core", dependencies.first().groupId)
    }

    @Test
    fun `fails on invalid gav`() {
        val input = Files.createTempFile("krawler", ".txt")
        Files.writeString(input, "androidx.core:core-ktx")

        assertThrows(IllegalArgumentException::class.java) {
            DependencyInputReader().read(input)
        }
    }
}
