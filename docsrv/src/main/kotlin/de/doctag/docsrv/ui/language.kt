package de.doctag.docsrv.ui

import com.github.salomonbrys.kotson.fromJson
import io.ktor.request.*
import kotlinx.serialization.json.JsonNull
import kweb.WebBrowser
import kweb.logger
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.util.gson
import kweb.util.random
import java.time.Duration
import java.util.*

data class LanguageResponse(val language: String)

data class SupportedLocale(
    val browserLanguageIso2: String,
    val locale: Locale,
    val flag: FomanticUIClasses
)

val supportedLocales = listOf(
    SupportedLocale("de", Locale.GERMANY, fomantic.ui.langDe),
    SupportedLocale("en", Locale.ENGLISH, fomantic.ui.langGb),
    SupportedLocale("es", Locale("ES"), fomantic.ui.langEs),
    SupportedLocale("it", Locale.ITALIAN, fomantic.ui.langIt),
    SupportedLocale("fr", Locale.FRENCH, fomantic.ui.langFr),
    SupportedLocale("pl", Locale("PL"), fomantic.ui.langPl),
    SupportedLocale("cz", Locale("CZ"), fomantic.ui.langCz),
)


private fun parseBrowserLanguage(langStr: String): Locale {
    val iso2Code = langStr.split("-")[0].toLowerCase()
    return supportedLocales.find { it.browserLanguageIso2 == iso2Code }?.locale ?: Locale.ENGLISH
}

fun ApplicationRequest.parseLanguageHeaders(): Locale{
    val uiLanguage = this.cookies.get("UI_LANG")?.trim()?.trim('"')

    return if (uiLanguage == null) {
        val langHeader = this.headers.get("Accept-Language")
        val language = langHeader?.let { parseBrowserLanguage(langHeader)} ?: Locale.ENGLISH

        //logger.info("Language string is ${langHeader}. Detected Language: ${language}")



        language

    } else {
        //logger.info("UI lang cookie is ${uiLanguage}")
        supportedLocales.find { it.browserLanguageIso2 == uiLanguage }?.locale ?: Locale.ENGLISH
    }
}

fun WebBrowser.getOrDetectBrowserLanguage(): Locale {
    return this.httpRequestInfo.request.parseLanguageHeaders()
}

fun Locale.getFomanticUiClass() : FomanticUIClasses {
    return supportedLocales.find{it.locale == this}?.flag ?: fomantic.ui.langGb
}

fun WebBrowser.setLanguage(lang: SupportedLocale) {
    logger.info("Setting UI language to ${lang.browserLanguageIso2}")
    doc.cookie.remove("UI_LANG")
    doc.cookie.set("UI_LANG", lang.browserLanguageIso2, expires = Duration.ofDays(14), path="\"/\"")
}