package com.arclights.dbpediaasker

import com.google.inject.Binder
import com.hubspot.dropwizard.guicier.DropwizardAwareModule

class DBpediaAskerModule : DropwizardAwareModule<DBpediaAskerConfiguration>() {
    override fun configure(binder: Binder) {
        getResources().forEach { binder.bind(it) }
        getManaged().forEach { binder.bind(it) }
    }

}