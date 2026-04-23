package dev.nevack.krawler.input

import dev.nevack.krawler.model.Gav
import java.nio.file.Path
import kotlin.io.path.readLines

class DependencyInputReader {
    fun read(path: Path): List<Gav> = path.readLines()
        .mapIndexedNotNull { index, line ->
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() || trimmed.startsWith("#") -> null
                else -> Gav.parse(trimmed, index + 1)
            }
        }
}
