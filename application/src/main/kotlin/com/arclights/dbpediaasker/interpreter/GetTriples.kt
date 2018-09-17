package com.arclights.dbpediaasker.interpreter

import com.arclights.dbpediaasker.commons.NamedEntities
import com.arclights.dbpediaasker.commons.NamedEntity
import com.arclights.dbpediaasker.commons.URI
import com.arclights.dbpediaasker.commons.triple.CreateTriples
import org.maltparser.core.exception.MaltChainedException
import org.maltparser.core.symbol.SymbolTable
import org.maltparser.core.syntaxgraph.DependencyStructure
import org.maltparser.core.syntaxgraph.node.DependencyNode
import org.slf4j.LoggerFactory
import java.util.ArrayList

private val logger = LoggerFactory.getLogger("GetTriples")

/**
 * Generates triples from the dependency structure of the question
 *
 * @param ds
 *            - The dependency structure of the question
 * @param NEs
 *            - The named entities
 * @return
 * @throws MaltChainedException
 */
fun getTriples(ds: DependencyStructure, NEs: NamedEntities): List<Triple<NamedEntity, URI, Any?>> {
    logger.debug("Creating triples...")
    val visitedNodes = ArrayList<Int>()
    return (1 until ds.highestDependencyNodeIndex)
            .asSequence()
            .map { i ->
                val currentNode = ds.getDependencyNode(i)
                if (currentNode.posTag() == "PM" && !visitedNodes.contains(i)) {
                    var prevProperNouns = listOf(currentNode)
                    visitedNodes.add(i)
                    for (j in i + 1..ds.highestDependencyNodeIndex) {
                        if (ds.getDependencyNode(j).posTag() == "PM") {
                            visitedNodes.add(j)
                            prevProperNouns = prevProperNouns.plus(ds.getDependencyNode(j))
                        } else {
                            break
                        }
                    }
                    val entity = prevProperNouns.joinToString(" ") { it.lemma() }

                    NEs[entity.toLowerCase()]?.let {
                        val tag = CreateTriples.findTag(prevProperNouns[0].head, ds)
                        Triple(it, URI("http://aakerber.net/tags/" + tag!!), null)
                    }

                } else {
                    null
                }
            }
            .toList()
            .filterNotNull()
}

private fun DependencyNode.posTag() = getLabelSymbol(labelTypes.toTypedArray()[4] as SymbolTable)
private fun DependencyNode.lemma() = getLabelSymbol(labelTypes.toTypedArray()[2] as SymbolTable)

fun Triple<NamedEntity, URI, Any?>.label() = first.dbPediaURI.fullURI!!