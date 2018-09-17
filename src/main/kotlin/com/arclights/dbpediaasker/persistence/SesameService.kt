package com.arclights.dbpediaasker.persistence

import com.arclights.dbpediaasker.namedEnteties.NamedEntity
import com.arclights.dbpediaasker.triple.URI
import com.google.inject.Inject
import io.dropwizard.lifecycle.Managed

class SesameService @Inject constructor(private val sesameDAO: SesameDAO) : Managed {
    override fun start() {
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Searches the Sesame database created by the question processor
     *
     * @param t
     * - The Triple to search for
     * @return
     */
    fun search(triple: Triple<NamedEntity, URI, Any?>):String? =
            sesameDAO.search(triple) ?: sesameDAO.searchAlt(triple)
}