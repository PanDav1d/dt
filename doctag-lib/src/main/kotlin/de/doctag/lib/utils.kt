package de.doctag.lib

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun getJackson() : ObjectMapper {
    val mapper = jacksonObjectMapper()
    mapper.apply {
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })

        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        registerModule(JavaTimeModule())
    }



    return mapper
}