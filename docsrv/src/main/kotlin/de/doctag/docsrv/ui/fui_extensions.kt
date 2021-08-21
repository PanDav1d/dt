package de.doctag.docsrv.ui

import kweb.*
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.plugins.fomanticUI.fomantic

val FomanticUIClasses.lock : FomanticUIClasses
    get() {
        classes("lock")
        return this
    }

val FomanticUIClasses.fileExport : FomanticUIClasses
    get() {
        classes("file export")
        return this
    }

val FomanticUIClasses.paw : FomanticUIClasses
    get() {
        classes("paw")
        return this
    }


val FomanticUIClasses.server : FomanticUIClasses
    get() {
        classes("server")
        return this
    }

val FomanticUIClasses.codeBranch : FomanticUIClasses
    get() {
        classes("code branch")
        return this
    }

val FomanticUIClasses.tertiary : FomanticUIClasses
    get() {
        classes("tertiary")
        return this
    }

val FomanticUIClasses.cog : FomanticUIClasses
    get() {
        classes("cog")
        return this
    }

val FomanticUIClasses.key : FomanticUIClasses
    get() {
        classes("key")
        return this
    }

val FomanticUIClasses.paperclip : FomanticUIClasses
    get() {
        classes("paperclip")
        return this
    }

val FomanticUIClasses.calendarDay : FomanticUIClasses
    get() {
        classes("calendar day")
        return this
    }

val FomanticUIClasses.addressCard : FomanticUIClasses
    get() {
        classes("address card")
        return this
    }

val FomanticUIClasses.tags : FomanticUIClasses
    get() {
        classes("tags")
        return this
    }

val FomanticUIClasses.selectable : FomanticUIClasses
    get() {
        classes("selectable")
        return this
    }

val FomanticUIClasses.doubling : FomanticUIClasses
    get() {
        classes("doubling")
        return this
    }

val FomanticUIClasses.required : FomanticUIClasses
    get() {
        classes("required")
        return this
    }

fun FomanticUIClasses.required(isRequired: Boolean):FomanticUIClasses{
    if(isRequired){
        classes("required")
    }
    return this
}

fun FomanticUIClasses.inline(isInline: Boolean):FomanticUIClasses{
    if(isInline){
        classes("inline")
    }
    return this
}

val FomanticUIClasses.upload : FomanticUIClasses
    get() {
        classes("upload")
        return this
    }

val FomanticUIClasses.toast : FomanticUIClasses
    get() {
        classes("toast")
        return this
    }

val FomanticUIClasses.sub : FomanticUIClasses
    get() {
        classes("sub")
        return this
    }

val FomanticUIClasses.toastContainer : FomanticUIClasses
    get() {
        classes("toast-container")
        return this
    }

val FomanticUIClasses.default : FomanticUIClasses
    get() {
        classes("default")
        return this
    }

val FomanticUIClasses.qrcode : FomanticUIClasses
    get() {
        classes("qrcode")
        return this
    }

val FomanticUIClasses.collapsing : FomanticUIClasses
    get() {
        classes("collapsing")
        return this
    }

val FomanticUIClasses.exclamationCircle : FomanticUIClasses
    get(){
        classes("exclamation circle")
        return this
    }

val FomanticUIClasses.spinner : FomanticUIClasses
    get(){
        classes("spinner fa-spin")
        return this
    }

val FomanticUIClasses.pdf : FomanticUIClasses
    get() {
        classes("pdf")
        return this
    }

val FomanticUIClasses.file : FomanticUIClasses
    get() {
        classes("file")
        return this
    }

val FomanticUIClasses.eye : FomanticUIClasses
    get() {
        classes("eye")
        return this
    }

val FomanticUIClasses.folder : FomanticUIClasses
    get() {
        classes("folder")
        return this
    }

val FomanticUIClasses.open : FomanticUIClasses
    get() {
        classes("open")
        return this
    }

val FomanticUIClasses.star : FomanticUIClasses
    get() {
        classes("star")
        return this
    }

val FomanticUIClasses.filter : FomanticUIClasses
    get() {
        classes("filter")
        return this
    }

val FomanticUIClasses.calendar : FomanticUIClasses
    get() {
        classes("calendar")
        return this
    }

val FomanticUIClasses.starOutline : FomanticUIClasses
    get() {
        classes("star outline")
        return this
    }

val FomanticUIClasses.rail : FomanticUIClasses
    get() {
        classes("rail")
        return this
    }

val FomanticUIClasses.syncAlternate : FomanticUIClasses
    get() {
        classes("sync alternate")
        return this
    }

val FomanticUIClasses.question : FomanticUIClasses
    get(){
        classes("question")
        return this
    }

fun FomanticUIClasses.withColor(color: String?):FomanticUIClasses{
    if(color != null) {
        classes(color)
    }
    return this
}

fun FomanticUIClasses.active(isActive: Boolean) = withOptionalAttribute("active", isActive)

fun FomanticUIClasses.checked(isChecked: Boolean) = withOptionalAttribute("checked", isChecked)

fun FomanticUIClasses.visible(isVisible: Boolean) = withOptionalAttribute("visible", isVisible)

fun FomanticUIClasses.negative(isNegative: Boolean) = withOptionalAttribute("negative", isNegative)

fun FomanticUIClasses.warning(isWarning: Boolean) = withOptionalAttribute("warning", isWarning)

fun FomanticUIClasses.info(isInfo: Boolean) = withOptionalAttribute("info", isInfo)

fun FomanticUIClasses.inverted(isInverted: Boolean) = withOptionalAttribute("inverted", isInverted)

fun FomanticUIClasses.positive(isPositive: Boolean)= withOptionalAttribute("positive", isPositive)

fun FomanticUIClasses.disabled(isDisabled: Boolean) = withOptionalAttribute("disabled", isDisabled)

fun FomanticUIClasses.loading(isLoading: Boolean) = withOptionalAttribute("loading", isLoading)

fun FomanticUIClasses.withOptionalAttribute(name: String, isEnabled: Boolean) : FomanticUIClasses {
    if(isEnabled){
        classes(name)
    }
    else{
        removeClass(name)
    }
    return this
}


fun Map<String, Any>.removeClass(cssClassName: String): Map<String, Any> {

    val classAttributeValue = get("class")
    val existing: List<String> = when (classAttributeValue) {
        is String -> classAttributeValue.split(' ').filter { it!=cssClassName }
        else -> listOf()
    }
    // TODO: This is inefficient when classes() is called multiple times
    return set("class", (existing).joinToString(separator = " "))

}

enum class DisplayMessageKind{
    Error,
    Warning,
    Info,
    Success
}

data class UserMessage(val kind:DisplayMessageKind, val header:String, val text:String)


fun Element.withPopup(title:String?, content:String?, distanceAway: Int = 60){
    setAttributeRaw("data-position", "top left")

    browser.execute("""
        $('#${this@withPopup.id}')
          .popup({
            position : 'top left',
            title    : '${title?:""}',
            content  : '${content?:""}',
            forcePosition : true,
            distanceAway: $distanceAway
          })
        ;
    """.trimIndent())

}

fun ElementCreator<*>.displayMessage(msg: UserMessage){
    div(fomantic.ui.message
            .negative(msg.kind == DisplayMessageKind.Error)
            .positive(msg.kind == DisplayMessageKind.Success)
            .warning(msg.kind == DisplayMessageKind.Warning)
            .info(msg.kind == DisplayMessageKind.Info)
    ).new {
        div(fomantic.ui.header).text(msg.header)
        p().text(msg.text)
    }
    div(fomantic.ui.divider.hidden)
}

