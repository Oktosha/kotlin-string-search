package com.example.search

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import java.io.BufferedReader
import java.io.FileReader
import java.nio.file.Path
import kotlin.io.path.walk
import kotlin.text.Regex.Companion.escape

interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}

data class OccurrenceData(
    override val file: Path,
    override val line: Int,
    override val offset: Int) : Occurrence {}

@OptIn(ExperimentalCoroutinesApi::class)
fun searchForTextOccurrences(
    stringToSearch: String,
    directory: Path
): Flow<Occurrence> {
    val regex = Regex(escape(stringToSearch))
    return directory.walk().asFlow().map<Path, Flow<Occurrence>> { path ->
        try {
            val lines = BufferedReader(FileReader(path.toFile())).lineSequence().asFlow()
            val matchedLines = lines.withIndex().map {
                val matches = regex.findAll(it.value)
                val occurrences = matches.map { match -> OccurrenceData(path, it.index + 1, match.range.first()) as Occurrence }
                return@map occurrences.asFlow()
            }
            return@map matchedLines.flattenConcat()
        } catch (_: Exception) {
            // the decision is to ignore entries we have trouble reading
            return@map emptyFlow<Occurrence>()
        }
    }.flattenConcat()
}