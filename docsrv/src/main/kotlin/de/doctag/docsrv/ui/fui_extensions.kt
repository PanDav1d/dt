package de.doctag.docsrv.ui

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

val FomanticUIClasses.star : FomanticUIClasses
    get() {
        classes("star")
        return this
    }

val FomanticUIClasses.starOutline : FomanticUIClasses
    get() {
        classes("star outline")
        return this
    }

fun FomanticUIClasses.withColor(color: String?):FomanticUIClasses{
    if(color != null) {
        classes(color)
    }
    return this
}

fun FomanticUIClasses.active(isActive: Boolean):FomanticUIClasses{
    if(isActive){
        classes("active")
    }
    return this
}

fun FomanticUIClasses.checked(isChecked: Boolean):FomanticUIClasses{
    if(isChecked){
        classes("checked")
    }
    return this
}

fun FomanticUIClasses.visible(isVisible: Boolean):FomanticUIClasses{
    if(isVisible){
        classes("visible")
    }
    return this
}

fun FomanticUIClasses.negative(isNegative: Boolean):FomanticUIClasses{
    if(isNegative){
        classes("negative")
    }
    return this
}

fun FomanticUIClasses.warning(isWarning: Boolean):FomanticUIClasses{
    if(isWarning){
        classes("warning")
    }
    return this
}

fun FomanticUIClasses.info(isInfo: Boolean):FomanticUIClasses{
    if(isInfo){
        classes("info")
    }
    return this
}

fun FomanticUIClasses.inverted(isInverted: Boolean): FomanticUIClasses{
    if(isInverted){
        classes("inverted")
    }
    return this
}

fun FomanticUIClasses.positive(isPositive: Boolean):FomanticUIClasses{
    if(isPositive){
        classes("positive")
    }
    return this
}

fun FomanticUIClasses.disabled(isDisabled: Boolean):FomanticUIClasses{
    if(isDisabled){
        classes("positive")
    }
    return this
}

fun FomanticUIClasses.loading(isLoading: Boolean):FomanticUIClasses{
    if(isLoading){
        classes("loading")
    }
    return this
}


enum class DisplayMessageKind{
    Error,
    Warning,
    Info,
    Success
}

data class UserMessage(val kind:DisplayMessageKind, val header:String, val text:String)


fun Element.withPopup(title:String?, content:String?){
    content?.let {
        setAttributeRaw("data-content", content)
    }
    title?.let {
        setAttributeRaw("data-title", title)
    }

    browser.execute("""
        $('#${this@withPopup.id}')
          .popup()
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

