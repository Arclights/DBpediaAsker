package com.arclights.dbpediaasker

import io.dropwizard.lifecycle.Managed
import org.reflections.Reflections
import javax.ws.rs.Path

fun getResources() = Reflections("dbpediaasker").getTypesAnnotatedWith(Path::class.java)
fun getManaged() = Reflections("dbpediaasker").getSubTypesOf(Managed::class.java)