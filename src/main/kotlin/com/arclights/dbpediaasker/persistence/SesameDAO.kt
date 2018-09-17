package com.arclights.dbpediaasker.persistence

import com.arclights.dbpediaasker.interpreter.label
import com.arclights.dbpediaasker.namedEnteties.NamedEntity
import com.arclights.dbpediaasker.triple.URI
import com.google.inject.Inject
import io.dropwizard.lifecycle.Managed
import org.openrdf.query.QueryLanguage
import org.openrdf.repository.RepositoryConnection
import org.openrdf.repository.http.HTTPRepository
import org.slf4j.LoggerFactory

class SesameDAO @Inject constructor() : Managed {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var repo: HTTPRepository
    private lateinit var con: RepositoryConnection

    override fun start() {
        repo = HTTPRepository("http://localhost:8090/openrdf-sesame/", "solution")
        con = repo.connection
    }

    override fun stop() {
        con.close()
    }


    fun search(triple: Triple<NamedEntity, URI, Any?>) =
            getDbQuery(triple)
                    .let {
                        logger.debug("Sesame Query: $it")
                        with(
                                con
                                        .prepareTupleQuery(QueryLanguage.SPARQL, it)
                                        .evaluate()
                        ) {
                            when {
                                hasNext() -> next().getValue("name").toString()
                                else -> null
                            }
                        }
                    }

    fun searchAlt(triple: Triple<NamedEntity, URI, Any?>): String? =
            getAltDbQuery(triple)
                    .let {
                        logger.debug("Sesame Query: $it")
                        with(
                                con
                                        .prepareTupleQuery(QueryLanguage.SPARQL, it)
                                        .evaluate()
                        ) {
                            when {
                                hasNext() -> next().getValue("name").toString().split('/').last()
                                else -> null
                            }
                        }
                    }


    /**
     * Generates the query to search the local database and trying to get the
     * potential name of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @return
     */
    private fun getDbQuery(t: Triple<NamedEntity, URI, Any?>): String =
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
    private fun getAltDbQuery(t: Triple<NamedEntity, URI, Any?>): String =
            "PREFIX tags:<http://aakerber.net/tags/>\n" +
                    "PREFIX dbp:<http://dbpedia.org/resource/>\n" +
                    "PREFIX dbpporp:<http://dbpedia.org/property/>\n" +
                    "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "select ?o {${t.first.dbPediaURI.queryVersion} <${t.label()}> ?o .}"
}