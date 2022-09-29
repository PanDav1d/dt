package com.logenios.xconvert.base

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.PathNotFoundException
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


val logger = LoggerFactory.getLogger(I18n::class.java)

class TranslationLoader(
    private val jsonDoc: Any
) {
    constructor(language: Locale) : this(
        jsonDoc = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).jsonProvider()
            .parse(
                Utils.getMergedJsonFiles(
                    "/locales/${
                        language.language.toLowerCase()
                    }"
                )
            )

    )

    constructor(file: File) : this(
        jsonDoc = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL).jsonProvider()
            .parse(
                file.readText()
            )
    )

    fun readPath(path: String): String? = try {
        JsonPath.read(jsonDoc, path)
    } catch (ce: ClassCastException) {
        logger.error(ce.message ?: "")
        logger.error("Path: $path")
        null
    } catch (e: PathNotFoundException) {
        null
    }
}

object I18n {

    private val translations: MutableMap<Locale, TranslationLoader> = mutableMapOf()


    fun t(
        path: String,
        text: String,
        language: Locale = Locale.GERMANY,
        additionalReplacementContextMap: Map<String, String>? = null
    ): String {
        val fixedText = text.fixWhitespaceAndOtherCrap()


        val localeDefaultGermanText = readJsonPathValueFromLocaleFile(Locale.GERMANY, path)
        val localizedText = if (localeDefaultGermanText != null) {
                val targetLocaleText = readJsonPathValueFromLocaleFile(language, path)
                if (!targetLocaleText.isNullOrBlank()) {
                    enhanceTargetLocaleWithParams(
                        targetLocaleText,
                        localeDefaultGermanText,
                        fixedText,
                        additionalReplacementContextMap
                    )
                } else {
                    logger.debug("Could not find path $path in targetLocaleText")
                    fixedText
                }
            } else {
                logger.debug("Could not find path $path in localeDefaultGermanText")
                fixedText
            }


        if (localizedText.contains("\$")) {
           logger.error("localizedText contains \$ after processing")
        }

        return localizedText
    }

    private fun enhanceTargetLocaleWithParams(
        targetLocaleText: String,
        germanDefaultText: String,
        defaultGermanTextWithFilledParams: String,
        additionalReplacementContextMap: Map<String, String>?
    ): String {
        val params = extractParams(germanDefaultText, defaultGermanTextWithFilledParams)
        var result = targetLocaleText
        params.forEach {
            result = result.replace(it.key, it.value)
        }
        additionalReplacementContextMap?.forEach {
            result = result.replace(it.key, it.value)
        }
        return result
    }

    val paramsRegex = Regex("""\$\{.*?}|(\$[^\s]+)""")

    fun splitByParams(text: String): List<String> {
        return text.split(paramsRegex)
    }

    fun extractParams(germanText: String, defaultGermanTextWithFilledParams: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val splittedByParams = splitByParams(germanText)
        splittedByParams.forEachIndexed { index, startString ->
            if (index + 1 < splittedByParams.size) {
                val stringAfter = splittedByParams[index + 1]
                val value = if (stringAfter.isNotEmpty()) {
                    defaultGermanTextWithFilledParams.substringAfter(startString).substringBefore(stringAfter)
                } else {
                    defaultGermanTextWithFilledParams.substringAfterLast(startString)
                }
                val key = if (stringAfter.isNotEmpty()) {
                    germanText.substringAfter(startString).substringBefore(stringAfter)
                } else {
                    germanText.substringAfterLast(startString)
                }
                params[key] = value
            }
        }
        return params
    }

    private fun readJsonPathValueFromLocaleFile(language: Locale, path: String): String? {
        return translations.getOrPut(language) {
            TranslationLoader(language)
        }.readPath(path)?.fixWhitespaceAndOtherCrap()
    }
}


fun String.fixWhitespaceAndOtherCrap(): String {
    return this.replace("\r\n", "").replace(Regex("\\s+"), " ").trim()
}

class LanguageFileDoesNotExistException(message: String) : Exception(message)


fun thisMessageWasAutomaticallyGenerated() = I18n.t(
    path = "Translation.thisMessageWasAutomaticallyGenerated",
    text = "Diese Nachricht wurde automatisch vom TRANSLATOR erstellt."
)
