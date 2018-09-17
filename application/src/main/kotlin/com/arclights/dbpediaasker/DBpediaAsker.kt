package com.arclights.dbpediaasker

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hubspot.dropwizard.guicier.GuiceBundle
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle

class DBpediaAsker : Application<DBpediaAskerConfiguration>() {
    override fun run(configuration: DBpediaAskerConfiguration?, environment: Environment?) {
    }

    override fun initialize(bootstrap: Bootstrap<DBpediaAskerConfiguration>) {
        super.initialize(bootstrap)
        bootstrap.objectMapper.registerKotlinModule()
        bootstrap.addBundle(
                GuiceBundle
                        .defaultBuilder(DBpediaAskerConfiguration::class.java)
                        .modules(DBpediaAskerModule())
                        .build()
        )
        bootstrap.addBundle(
                object : SwaggerBundle<DBpediaAskerConfiguration>() {
                    override fun getSwaggerBundleConfiguration(configuration: DBpediaAskerConfiguration) = configuration.swaggerBundleConfiguration
                }
        )
    }
}

fun main(args: Array<String>) {
    DBpediaAsker().run(*args)
}