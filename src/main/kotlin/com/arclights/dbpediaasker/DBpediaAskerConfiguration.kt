package com.arclights.dbpediaasker

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration

data class DBpediaAskerConfiguration(
        val port:Int,
        @JsonProperty("swagger") val swaggerBundleConfiguration:SwaggerBundleConfiguration
): Configuration()