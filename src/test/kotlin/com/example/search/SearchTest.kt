package com.example.search

import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SearchTest {
    @Test
    fun searchSources() {
        runBlocking {
            val occurrences = searchForTextOccurrences("println", Paths.get("./src"))
            occurrences.buffer().collect { println("${it.file} -- ${it.line}:${it.offset}") }
        }
    }

}