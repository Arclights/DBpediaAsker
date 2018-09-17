package com.arclights.dbpediaasker.api.question

import com.arclights.dbpediaasker.asker.AskerService
import com.codahale.metrics.annotation.Timed
import com.google.inject.Inject
import io.swagger.annotations.Api
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Api("Ask")
@Path("/ask")
@Produces(MediaType.APPLICATION_JSON)
class QuestionResource @Inject constructor(val askerService: AskerService) {

    @GET
    @Timed
    fun ask(@QueryParam("qeustion") question: String): String? {
        return askerService.ask(question)
    }

}