package dev.nevack.krawler.version

import dev.nevack.krawler.config.UpdateStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VersionStrategySelectorTest {
    private val selector = VersionStrategySelector()

    @Test
    fun `latest ignores prereleases for stable current version`() {
        val selected = selector.select(
            currentVersion = "1.0.0",
            versions = listOf("1.0.1", "1.1.0-beta01", "1.2.0-SNAPSHOT"),
            strategy = UpdateStrategy.LATEST,
        )

        assertEquals("1.0.1", selected)
    }

    @Test
    fun `latest minor keeps same major`() {
        val selected = selector.select(
            currentVersion = "1.2.3",
            versions = listOf("1.2.4", "1.9.0", "2.0.0"),
            strategy = UpdateStrategy.LATEST_MINOR,
        )

        assertEquals("1.9.0", selected)
    }

    @Test
    fun `latest patch keeps same major and minor`() {
        val selected = selector.select(
            currentVersion = "1.2.3",
            versions = listOf("1.2.4", "1.3.0", "2.0.0"),
            strategy = UpdateStrategy.LATEST_PATCH,
        )

        assertEquals("1.2.4", selected)
    }

    @Test
    fun `latest allows prereleases when current version is prerelease`() {
        val selected = selector.select(
            currentVersion = "1.2.0-beta01",
            versions = listOf("1.2.0-rc01", "1.2.0", "1.3.0-beta01"),
            strategy = UpdateStrategy.LATEST,
        )

        assertEquals("1.3.0-beta01", selected)
    }
}
