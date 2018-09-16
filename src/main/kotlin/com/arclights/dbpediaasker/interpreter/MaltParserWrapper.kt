package com.arclights.dbpediaasker.interpreter

import com.arclights.dbpediaasker.tools.DependencyStructureTool
import com.google.inject.Inject
import com.google.inject.Singleton
import io.dropwizard.lifecycle.Managed
import org.maltparser.MaltParserService
import org.maltparser.core.syntaxgraph.DependencyStructure
import org.slf4j.LoggerFactory

@Singleton
class MaltParserWrapper @Inject constructor() : Managed {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var maltParser: MaltParserService

    override fun start() {
        logger.info("Initiating MaltParser...")
        maltParser = MaltParserService()
        maltParser.initializeParserModel("-c swemalt-1.7.2 -m parse -w . -lfi parser.log") // TODO: Do something about the mco file having to be in the top of the file structure
        logger.info("MaltParser initialised")
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getDependecyStructure(taggedQuestion: List<TaggedString>): DependencyStructure {
        logger.debug("Extracting question...")
        val out = taggedQuestion.map { it.toConllString() }.let { maltParser.parse(it.toTypedArray()) }
        logger.debug(DependencyStructureTool.graphToString(out))
        return out
    }
}