package dev.nevack.mavenkrawler.version

import dev.nevack.mavenkrawler.config.UpdateStrategy
import org.apache.maven.artifact.versioning.ComparableVersion

class VersionStrategySelector {
    fun select(currentVersion: String, versions: Collection<String>, strategy: UpdateStrategy): String? {
        val currentComparable = ComparableVersion(currentVersion)
        val currentNumericCore = VersionClassifier.numericCore(currentVersion)
        val currentIsPrerelease = VersionClassifier.isPreRelease(currentVersion)

        return versions.asSequence()
            .distinct()
            .filter { ComparableVersion(it) > currentComparable }
            .filter { candidate -> isCandidateAllowed(candidate, strategy, currentNumericCore, currentIsPrerelease) }
            .maxByOrNull(::ComparableVersion)
    }

    private fun isCandidateAllowed(
        candidate: String,
        strategy: UpdateStrategy,
        currentNumericCore: NumericCore,
        currentIsPrerelease: Boolean,
    ): Boolean {
        val candidateNumericCore = VersionClassifier.numericCore(candidate)
        val candidateIsSnapshot = VersionClassifier.isSnapshot(candidate)
        val candidateIsPrerelease = VersionClassifier.isPreRelease(candidate)

        return when (strategy) {
            UpdateStrategy.LATEST -> {
                !candidateIsSnapshot && (!candidateIsPrerelease || currentIsPrerelease)
            }

            UpdateStrategy.LATEST_SNAPSHOT -> true
            UpdateStrategy.LATEST_MINOR -> {
                candidateNumericCore.major == currentNumericCore.major &&
                    !candidateIsSnapshot &&
                    (!candidateIsPrerelease || currentIsPrerelease)
            }

            UpdateStrategy.LATEST_PATCH -> {
                candidateNumericCore.major == currentNumericCore.major &&
                    candidateNumericCore.minor == currentNumericCore.minor &&
                    !candidateIsSnapshot &&
                    (!candidateIsPrerelease || currentIsPrerelease)
            }
        }
    }
}
