package com.arclights.dbpediaasker.asker

import com.arclights.dbpediaasker.commons.NamedEntity
import com.arclights.dbpediaasker.commons.ParseDbPediaURIs
import com.arclights.dbpediaasker.commons.ParseTagTranslations
import com.arclights.dbpediaasker.commons.URI
import com.google.inject.Inject
import com.google.inject.Singleton
import com.arclights.dbpediaasker.dbpedia.DBpediaService
import com.arclights.dbpediaasker.interpreter.MaltParserWrapper
import com.arclights.dbpediaasker.interpreter.TaggerWrapper
import com.arclights.dbpediaasker.interpreter.getTriples
import com.arclights.dbpediaasker.namedEntities.extractNamedEntities
import com.arclights.dbpediaasker.persistence.SesameService
import io.dropwizard.lifecycle.Managed
import org.slf4j.LoggerFactory

@Singleton
class AskerService @Inject constructor(
        private val maltParserWrapper: MaltParserWrapper,
        private val taggerWrapper: TaggerWrapper,
        private val dBpediaService: DBpediaService,
        private val sesameService: SesameService
) : Managed {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var dbpediaURIs: Map<String, String>
    private lateinit var tagTrans: Map<String, String>

    override fun start() {
        logger.info("Loading tagger...")
        logger.info("Tagger loaded")

        dbpediaURIs = ParseDbPediaURIs.parse()
        tagTrans = ParseTagTranslations.parse()
    }

    override fun stop() {
    }

    fun ask(question: String): String? {
        logger.debug(question)
        logger.debug("Tagging question...")
        val taggedQuestion = taggerWrapper.tag(question)
        logger.debug("Question tagged")
        logger.debug(taggedQuestion.toString())

        val NEs = extractNamedEntities(taggedQuestion)
        NEs.forEach {
            it.value.setIdentifiers(dbpediaURIs)
        }
        NEs.clearUp()
        logger.debug("Named entities extracted...")
        logger.debug("Named entities:")
        NEs.forEach { key, value ->
            logger.debug("$key->$value")
        }

        logger.debug("Retrieving dependency structure...")
        val ds = maltParserWrapper.getDependecyStructure(taggedQuestion)
        logger.debug("Derpendency structure retreived")

        logger.debug("Creating triples...")
        val triples = getTriples(ds, NEs)

        triples.forEach {
            println(tripleToString(it))
        }

        return formatString(getAnswer(triples, tagTrans))
    }

    private fun tripleToString(t: Triple<NamedEntity, URI, Any?>) = "${t.first} --- ${t.second} --> ${t.third}"

    /**
     * Returns an answer for the question, if there is one, by first searching
     * locally. If it doesn't find an answer there it proceeds to search DBpedia
     *
     * @param triples
     * - The incomplete triples that, when complete, can answer the
     * question
     * @param tagTrans
     * - The hashmap with the tag -> label translations
     * @return
     */
    private fun getAnswer(
            triples: List<Triple<NamedEntity, URI, Any?>>,
            tagTrans: Map<String, String>
    ): String =
            when {
                triples.isNotEmpty() ->
                    sesameService.search(triples.first())
                            ?: dBpediaService.searchDBpedia(triples.first(), tagTrans)
                            ?: "I don't know the answer to that"
                else -> "Error: Doesn't contain any named entity"
            }

    /**
     * Removes possible quotes from string. If there isn't any quotes the
     * original string is returned. Else if the string is surrounded only by
     * quotes, the quotes are removed, ie. "obi-wan kenobi". Else if the string
     * is surrounded by quotes and a language marker, the marker and the quotes
     * are remove, ie. "Moscow"@en.
     *
     * @param string
     * @return
     */
    private fun formatString(string: String): String =
            string
                    .replace('_', ' ')
                    .replace("\"", "")
                    .replace("@\\w{2}".toRegex(), "")
}