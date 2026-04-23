package dev.nevack.krawler.version

object VersionClassifier {
    private val prereleaseMarker = Regex("(?i)(alpha|beta|milestone|preview|rc|cr|m\\d|a\\d|b\\d)")

    fun isSnapshot(version: String): Boolean = version.contains("SNAPSHOT", ignoreCase = true)

    fun isPreRelease(version: String): Boolean = isSnapshot(version) || prereleaseMarker.containsMatchIn(version)

    fun numericCore(version: String): NumericCore {
        val match = Regex("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?").find(version)
            ?: return NumericCore(0, 0, 0)
        return NumericCore(
            major = match.groupValues[1].toInt(),
            minor = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0,
            patch = match.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0,
        )
    }
}

data class NumericCore(
    val major: Int,
    val minor: Int,
    val patch: Int,
)
