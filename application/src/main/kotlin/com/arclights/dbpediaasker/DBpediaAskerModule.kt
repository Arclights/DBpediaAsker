package com.arclights.dbpediaasker

import com.google.common.io.Files
import com.google.inject.Binder
import com.google.inject.Provides
import com.hubspot.dropwizard.guicier.DropwizardAwareModule
import java.io.File
import java.nio.charset.StandardCharsets

class DBpediaAskerModule : DropwizardAwareModule<DBpediaAskerConfiguration>() {
    override fun configure(binder: Binder) {
        getResources().forEach { binder.bind(it) }
        getManaged().forEach { binder.bind(it) }
    }

    @Provides
    fun getTaggerModel() = Files.asCharSource(File(configuration.taggerModel), StandardCharsets.UTF_8)

}