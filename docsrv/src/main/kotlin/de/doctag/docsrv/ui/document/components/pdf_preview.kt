package de.doctag.docsrv.ui.document.components

import kweb.ElementCreator
import kweb.canvas

fun ElementCreator<*>.renderPdfPreview(){
    element("script", mapOf("src" to "/ressources/pdf.js"))

    val canvas = canvas(420, 300).apply {
        this.setAttributeRaw("style", "border: 1px solid black;)")
    }

}