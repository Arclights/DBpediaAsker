package com.arclights.dbpediaasker.namedEntities

import com.arclights.dbpediaasker.interpreter.IOB.B
import com.arclights.dbpediaasker.interpreter.IOB.I
import com.arclights.dbpediaasker.interpreter.TaggedString
import com.arclights.dbpediaasker.namedEnteties.NamedEntities
import com.arclights.dbpediaasker.namedEnteties.NamedEntity
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger("ExtractNamedEntities")

/**
 * Extracts the named entities found by Stagger
 *
 * @param fileName
 * - Stagger output
 * @return - -The named entities
 */
fun extractNamedEntities(taggedQuestion: List<TaggedString>): NamedEntities {
    logger.debug("Extracting named entities...")
    val out = NamedEntities()
    taggedQuestion
            .filter { it.neTag in setOf(B, I) }
            .let { extractNE(it) }
            .forEach { out.put(it) }
    return out
}

private fun extractNE(parts: List<TaggedString>): List<NamedEntity> =
        when {
            parts.isEmpty() -> emptyList()
            parts.size > 1 && parts.first().neTag == B && parts[1].neTag == I ->
                listOf(
                        NamedEntity(parts.first().neType)
                                .put(parts.first().lemma)
                                .put(parts[1].lemma)
                )
                        .plus(extractNE(parts.drop(2)))
            parts.first().neTag == B ->
                listOf(
                        NamedEntity(parts.first().neType)
                                .put(parts.first().lemma)
                )
                        .plus(extractNE(parts.drop(1)))
            else -> throw IllegalStateException("Cannot extract named entity from tagged strings $parts")
        }