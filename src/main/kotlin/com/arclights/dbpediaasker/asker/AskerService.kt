package com.arclights.dbpediaasker.asker

import com.arclights.dbpediaasker.dbPedia.ParseDbPediaURIs
import com.arclights.dbpediaasker.namedEnteties.ExtractNamedEnteties
import com.arclights.dbpediaasker.namedEnteties.NamedEntity
import com.arclights.dbpediaasker.serverInterpreter.GetDependencyStructure
import com.arclights.dbpediaasker.serverInterpreter.GetTriples
import com.arclights.dbpediaasker.serverInterpreter.ParseTagTranslations
import com.arclights.dbpediaasker.serverInterpreter.ServerTagger
import com.arclights.dbpediaasker.triple.Triple
import com.google.inject.Inject
import com.google.inject.Singleton
import io.dropwizard.lifecycle.Managed
import org.openrdf.query.*
import org.openrdf.repository.RepositoryConnection
import org.openrdf.repository.RepositoryException
import org.openrdf.repository.http.HTTPRepository
import org.openrdf.repository.sparql.SPARQLRepository
import java.io.PrintWriter
import java.util.*

@Singleton
class AskerService @Inject constructor() : Managed {

    private lateinit var tagger: ServerTagger
    private lateinit var dbpediaURIs: Map<String, String>
    private lateinit var tagTrans: Map<String, String>

    override fun start() {
        println("Loading tagger...")
        tagger = ServerTagger("configs/swedish.bin")
        println("Tagger loaded")

        dbpediaURIs = ParseDbPediaURIs.parse()
        tagTrans = ParseTagTranslations.parse()
    }

    override fun stop() {
    }

    fun ask(question: String): String? {
        println(question)
        println("Tagging question...")
        val inputFiles = ArrayList<String>()
        inputFiles.add("questionToTag.txt")
        tagger.tag(inputFiles)
        println("Question tagged")

        val NEs = ExtractNamedEnteties
                .extract("questionToTag.txt.conll")
        for (ne in NEs.values) {
            ne.setIdentifiers(dbpediaURIs)
        }
        NEs.clearUp()
        println("Named entities extracted...")
        println("Named entities:")
        for (key in NEs.keys) {
            println(key + "->" + NEs[key])
        }

        println("Retrieving dependency structure...")
        val ds = GetDependencyStructure
                .getStructure()
        println("Derpendency structure retreived")

        println("Creating triples...")
        val triples = GetTriples.get(ds!!, NEs)

        for (t in triples) {
            println(t)
        }

        return formatString(getAnswer(triples, tagTrans))
    }

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
    private fun getAnswer(triples: ArrayList<Triple>,
                          tagTrans: Map<String, String>): String? {
        var answer: String? = "Error: Doesn't contain any named entity"
        if (!triples.isEmpty()) {
            answer = searchlocalDb(triples[0])

            if (answer == null) {
                answer = searchDbPedia(triples[0], tagTrans)
            }

            if (answer == null) {
                answer = "I don't know the answer to that"
            }
        }

        return answer
    }

    /**
     * Searches the local database created by the question processor
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple to search for
     * @return
     */
    private fun searchlocalDb(t: Triple): String? {
        val repo = HTTPRepository(
                "http://aakerberg.net:8077/openrdf-sesame/", "solution")
        var con: RepositoryConnection? = null
        try {
            con = repo.connection
            var tupleQuery = con!!.prepareTupleQuery(QueryLanguage.SPARQL,
                    getLocalDbQuery(t))
            println("Local Query: " + getLocalDbQuery(t))

            var result = tupleQuery.evaluate()
            val bindingSet: BindingSet
            if (result.hasNext()) {
                bindingSet = result.next()
                return bindingSet.getValue("name").toString()
            } else {
                tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                        getAltLocalDbQuery(t))
                println("Local Alternative Query: " + getAltLocalDbQuery(t))
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
    private fun getLocalDbQuery(t: Triple): String {
        return ("PREFIX tags:<http://aakerber.net/tags/>\nPREFIX dbp:<http://dbpedia.org/resource/>\nPREFIX dbpporp:<http://dbpedia.org/property/>\nPREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nselect ?name {"
                + (t.s as NamedEntity).dbPediaURI.queryVersion
                + " <"
                + t.label
                + "> ?o .\n?o <http://dbpedia.org/property/name> ?name .}")
    }

    /**
     * Generates the query to search the local database without getting the
     * potential name of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @return
     */
    private fun getAltLocalDbQuery(t: Triple): String {
        return ("PREFIX tags:<http://aakerber.net/tags/>\nPREFIX dbp:<http://dbpedia.org/resource/>\nPREFIX dbpporp:<http://dbpedia.org/property/>\nPREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nselect ?o {"
                + (t.s as NamedEntity).dbPediaURI.queryVersion
                + " <" + t.label + "> ?o .}")
    }

    /**
     * Searches DBpedia for an answer
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun searchDbPedia(t: Triple,
                              tagTrans: Map<String, String>): String? {
        val repo = SPARQLRepository(
                "http://dbpedia.org/sparql/")
        var result: TupleQueryResult? = null
        try {
            repo.initialize()
            val con = repo.connection
            var tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                    getDbPediaQuery(t, tagTrans))
            println("Dbpedia Query: " + getDbPediaQuery(t, tagTrans))
            result = tupleQuery.evaluate()
            val bindingSet: BindingSet
            if (result!!.hasNext()) {
                bindingSet = result.next()
                return bindingSet.getValue("name").toString()
            } else {
                tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                        getAltDbPediaQuery(t, tagTrans))
                println("Dbpedia Alternative Query: " + getAltDbPediaQuery(t, tagTrans))
                result = tupleQuery.evaluate()
                if (result!!.hasNext()) {
                    bindingSet = result.next()
                    return bindingSet.getValue("name").toString()
                } else {
                    tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                            getAlt2DbPediaQuery(t, tagTrans))
                    println("Dbpedia Alternative Query: " + getAlt2DbPediaQuery(t, tagTrans))
                    result = tupleQuery.evaluate()
                    if (result!!.hasNext()) {
                        bindingSet = result.next()
                        val res = bindingSet.getValue("o").toString()
                                .split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        return res[res.size - 1]
                    }
                }
            }
        } catch (e: RepositoryException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: QueryEvaluationException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: MalformedQueryException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } finally {
            try {
                result?.close()
            } catch (e: QueryEvaluationException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }
        return null
    }

    /**
     * Generates the query to search DBpedia and trying to get the potential
     * name of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun getDbPediaQuery(t: Triple,
                                tagTrans: Map<String, String>): String {
        return ("select ?name {"
                + (t.s as NamedEntity).dbPediaURI.queryVersion
                + " <"
                + tagTrans[t.label]
                + "> ?o .\n?o <http://dbpedia.org/property/name> ?name .} LIMIT 1")
    }

    /**
     * Generates the query to search DBpedia and trying to get the potential
     * English name of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun getAltDbPediaQuery(t: Triple,
                                   tagTrans: Map<String, String>): String {
        return ("select ?name {"
                + (t.s as NamedEntity).dbPediaURI.queryVersion
                + " <"
                + tagTrans[t.label]
                + "> ?o .\n?o <http://dbpedia.org/property/enName> ?name .} LIMIT 1")
    }

    /**
     * Generates the query to search DBpedia without getting the potential name
     * of the entity
     *
     * @param t
     * - The com.arclights.dbpediaasker.triple
     * @param tagTrans
     * - The tag -> label translations
     * @return
     */
    private fun getAlt2DbPediaQuery(t: Triple,
                                    tagTrans: Map<String, String>): String {
        return ("select ?o {"
                + (t.s as NamedEntity).dbPediaURI.queryVersion
                + " <" + tagTrans[t.label] + "> ?o .} LIMIT 1")
    }

    /**
     * Removes possible quotes from string. If there isn't any quotes the
     * original string is returned. Else if the string is surrounded only by
     * quotes, the quotes are removed, ie. "obi-wan kenobi". Else if the string
     * is surrounded by quotes and a language marker, the marker and the quotes
     * are remove, ie. "Moscow"@en.
     *
     * @param in
     * @return
     */
    private fun formatString(`in`: String?): String? {
        if (`in` != null && `in`.isNotEmpty()) {
            if (`in`[0] == '\"') {
                return if (`in`[`in`.length - 1] == '\"') {
                    `in`.substring(1, `in`.length - 1)
                } else `in`.substring(1, `in`.length - 4)
            }
            return `in`.replace('_', ' ')
        }
        return `in`
    }
}