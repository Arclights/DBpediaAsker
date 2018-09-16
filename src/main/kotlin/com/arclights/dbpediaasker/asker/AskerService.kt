package com.arclights.dbpediaasker.asker

import com.arclights.dbpediaasker.dbPedia.ParseDbPediaURIs
import com.arclights.dbpediaasker.dbpedia.DBpediaService
import com.arclights.dbpediaasker.interpreter.MaltParserWrapper
import com.arclights.dbpediaasker.interpreter.TaggerWrapper
import com.arclights.dbpediaasker.interpreter.getTriples
import com.arclights.dbpediaasker.interpreter.label
import com.arclights.dbpediaasker.namedEnteties.NamedEntity
import com.arclights.dbpediaasker.namedEntities.extractNamedEntities
import com.arclights.dbpediaasker.serverInterpreter.ParseTagTranslations
import com.arclights.dbpediaasker.triple.URI
import com.google.inject.Inject
import com.google.inject.Singleton
import io.dropwizard.lifecycle.Managed
import org.openrdf.query.BindingSet
import org.openrdf.query.MalformedQueryException
import org.openrdf.query.QueryEvaluationException
import org.openrdf.query.QueryLanguage
import org.openrdf.repository.RepositoryConnection
import org.openrdf.repository.RepositoryException
import org.openrdf.repository.http.HTTPRepository
import org.slf4j.LoggerFactory

@Singleton
class AskerService @Inject constructor(
        private val maltParserWrapper: MaltParserWrapper,
        private val taggerWrapper: TaggerWrapper,
        private val dBpediaService: DBpediaService
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
            if (triples.isNotEmpty()) {
                searchlocalDb(triples.first()) ?: dBpediaService.searchDBpedia(triples.first(), tagTrans)
                ?: "I don't know the answer to that"
            } else {
                "Error: Doesn't contain any named entity"
            }

    /**
     * Searches the local database created by the question processor
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple to search for
     * @return
     */
    private fun searchlocalDb(t: Triple<NamedEntity, URI, Any?>): String? {
        val repo = HTTPRepository("http://localhost:8090/openrdf-sesame/", "solution")
        var con: RepositoryConnection? = null
        try {
            con = repo.connection
            var tupleQuery = con!!.prepareTupleQuery(QueryLanguage.SPARQL,
                    getLocalDbQuery(t))
            logger.debug("Local Query: " + getLocalDbQuery(t))

            var result = tupleQuery.evaluate()
            val bindingSet: BindingSet
            if (result.hasNext()) {
                bindingSet = result.next()
                return bindingSet.getValue("name").toString()
            } else {
                tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, getAltLocalDbQuery(t))
                logger.debug("Local Alternative Query: " + getAltLocalDbQuery(t))
                result = tupleQuery.evaluate()
                if (result.hasNext()) {
                    bindingSet = result.next()
                    val res = bindingSet.getValue("o").toString()
                            .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return res[res.size - 1]
                }
            }
        } catch (e: RepositoryException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: MalformedQueryException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: QueryEvaluationException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } finally {
            try {
                con?.close()
                repo.shutDown()
            } catch (e: RepositoryException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }
        return null
    }

    /**
     * Generates the query to search the local database and trying to get the
     * potential name of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @return
     */
    private fun getLocalDbQuery(t: Triple<NamedEntity, URI, Any?>): String =
            "PREFIX tags:<http://aakerber.net/tags/>\n" +
                    "PREFIX dbp:<http://dbpedia.org/resource/>\n" +
                    "PREFIX dbpporp:<http://dbpedia.org/property/>\n" +
                    "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?name {${t.first.dbPediaURI.queryVersion} <${t.label()}> ?o .\n" +
                    "?o <http://dbpedia.org/property/name> ?name .}"

    /**
     * Generates the query to search the local database without getting the
     * potential name of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @return
     */
    private fun getAltLocalDbQuery(t: Triple<NamedEntity, URI, Any?>): String =
            "PREFIX tags:<http://aakerber.net/tags/>\n" +
                    "PREFIX dbp:<http://dbpedia.org/resource/>\n" +
                    "PREFIX dbpporp:<http://dbpedia.org/property/>\n" +
                    "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?o {${t.first.dbPediaURI.queryVersion} <${t.label()}> ?o .}"


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