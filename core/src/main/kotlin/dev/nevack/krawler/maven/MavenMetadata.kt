package dev.nevack.krawler.maven

data class MavenMetadata(
    val versions: List<String> = emptyList(),
    val latest: String? = null,
    val release: String? = null,
) {
    fun allVersions(): List<String> = buildList {
        addAll(versions)
        latest?.let(::add)
        release?.let(::add)
    }.distinct()
}
