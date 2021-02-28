package de.doctag.lib

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.streams.asSequence

fun getJackson() : ObjectMapper {
    val mapper = jacksonObjectMapper()
    mapper.apply {
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })

        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        registerModule(JavaTimeModule())
    }

    return mapper
}

private val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
fun generateRandomString(length: Long) = java.util.Random().ints(length, 0, source.length).asSequence().map(source::get).joinToString("")


fun String.fixHttps() = replace("https://127.0.0.1","http://127.0.0.1")
fun String.isUrl(): Boolean = this.startsWith("https://") || this.startsWith("http://127.")