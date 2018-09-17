package com.arclights.dbpediaasker.dbpedia

import com.arclights.dbpediaasker.external.dbpedia.DBpediaClient
import com.arclights.dbpediaasker.namedEnteties.NamedEntity
import com.arclights.dbpediaasker.triple.URI
import com.google.inject.Inject
import com.google.inject.Singleton
import io.dropwizard.lifecycle.Managed
import org.slf4j.LoggerFactory

@Singleton
class DBpediaService @Inject constructor(private val dBpediaClient: DBpediaClient) : Managed {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun start() {
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    fun searchDBpedia(
            t: Triple<NamedEntity, URI, Any?>,
            tagTrans: Map<String, String>
    ): String? =
            dBpediaClient.search(t, tagTrans) ?: dBpediaClient.searchAlt1(t, tagTrans)
            ?: dBpediaClient.serachAlt2(t, tagTrans)
}