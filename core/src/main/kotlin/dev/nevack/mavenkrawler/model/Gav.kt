package dev.nevack.krawler.model

import kotlinx.serialization.Serializable

@Serializable
data class Gav(
    val groupId: String,
    val artifactId: String,
    val version: String,
) {
    val ga: String
        get() = "$groupId:$artifactId"

    val coordinate: String
        get() = "$ga:$version"

    companion object {
        fun parse(raw: String, lineNumber: Int? = null): Gav {
            val parts = raw.split(':')
            require(parts.size == 3 && parts.all { it.isNotBlank() }) {
                buildString {
                    append("Dependency must be in GAV format 'group:artifact:version'")
                    if (lineNumber != null) {
                        append(" at line ")
                        append(lineNumber)
                    }
                    append(": ")
                    append(raw)
                }
            }
            return Gav(parts[0], parts[1], parts[2])
        }
    }
}
