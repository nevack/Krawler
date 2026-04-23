package dev.nevack.mavenkrawler.model

import dev.nevack.mavenkrawler.config.UpdateStrategy
import java.time.Instant

data class AvailableUpdate(
    val dependency: Gav,
    val targetVersion: String,
    val repositories: List<String>,
)

data class CrawlReport(
    val strategy: UpdateStrategy,
    val checkedDependencies: Int,
    val updates: List<AvailableUpdate>,
    val generatedAt: Instant = Instant.now(),
)
