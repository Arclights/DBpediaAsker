package com.arclights.dbpediaasker.external.dbpedia

import com.arclights.dbpediaasker.commons.NamedEntity
import com.arclights.dbpediaasker.commons.URI
import com.arclights.dbpediaasker.interpreter.label
import com.google.inject.Inject
import com.google.inject.Singleton
import io.dropwizard.lifecycle.Managed
import org.openrdf.query.QueryLanguage
import org.openrdf.repository.RepositoryConnection
import org.openrdf.repository.sparql.SPARQLRepository
import org.slf4j.LoggerFactory

@Singleton
class DBpediaClient @Inject constructor() : Managed {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var connection: RepositoryConnection

    override fun start() {
        val repo = SPARQLRepository("http://dbpedia.org/sparql/")
        repo.initialize()
        connection = repo.connection
    }

    override fun stop() {
        connection.close()
    }

    fun search(
            triple: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String? {
        getDBpediaQuery(triple, tagTrans)
                .let {
                    logger.debug("Dbpedia Query: $it")
                    return with(
                            connection
                                    .prepareTupleQuery(QueryLanguage.SPARQL, it)
                                    .evaluate()
                    ) {
                        when {
                            hasNext() -> next().getValue("name").toString()
                            else -> null
                        }
                    }

                }
    }

    fun searchAlt1(
            triple: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String? {
        getAltDBpediaQuery(triple, tagTrans)
                .let {
                    logger.debug("Dbpedia Query: $it")
                    return with(
                            connection
                                    .prepareTupleQuery(QueryLanguage.SPARQL, it)
                                    .evaluate()
                    ) {
                        when {
                            hasNext() -> next().getValue("name").toString()
                            else -> null
                        }
                    }

                }
    }

    fun serachAlt2(
            triple: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String? {
        getAlt2DBpediaQuery(triple, tagTrans)
                .let {
                    logger.debug("Dbpedia Query: $it")
                    return with(
                            connection
                                    .prepareTupleQuery(QueryLanguage.SPARQL, it)
                                    .evaluate()
                    ) {
                        when {
                            hasNext() -> next().getValue("o").toString().split("/").last()
                            else -> null
                        }
                    }

                }
    }

    /**
     * Generates the query to search DBpedia and trying to get the potential
     * name of the entity
     *
     * @param t
     * - The dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun getDBpediaQuery(
            t: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String =
            "select ?name {${t.first.dbPediaURI.queryVersion} <${tagTrans[t.label()]}> ?o .\n" +
                    "?o <http://dbpedia.org/property/name> ?name .} LIMIT 1"

    /**
     * Generates the query to search DBpedia and trying to get the potential
     * English name of the entity
     *
     * @param t
     * - The dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun getAltDBpediaQuery(
            t: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String =
            "select ?name {${t.first.dbPediaURI.queryVersion} <${tagTrans[t.label()]}> ?o .\n" +
                    "?o <http://dbpedia.org/property/enName> ?name .} LIMIT 1"

    /**
     * Generates the query to search DBpedia without getting the potential name
     * of the entity
     *
     * @param t
     * - The dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun getAlt2DBpediaQuery(
            t: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String =
            "select ?o {${t.first.dbPediaURI.queryVersion} <${tagTrans[t.label()]}> ?o .} LIMIT 1"
}