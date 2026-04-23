package dev.nevack.mavenkrawler.repo

object GroupPatternMatcher {
    fun matches(groupId: String, pattern: String): Boolean = when {
        pattern.endsWith("*") -> groupId.startsWith(pattern.removeSuffix("*"))
        pattern.endsWith(".") -> groupId.startsWith(pattern)
        else -> groupId == pattern
    }
}
