package de.doctag.keysrv.ui

import kweb.classes
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.set

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

val FomanticUIClasses.tertiary : FomanticUIClasses
    get() {
        classes("tertiary")
        return this
    }

val FomanticUIClasses.random : FomanticUIClasses
    get() {
        classes("random")
        return this
    }

val FomanticUIClasses.cog : FomanticUIClasses
    get() {
        classes("cog")
        return this
    }

val FomanticUIClasses.selectable : FomanticUIClasses
    get() {
        classes("selectable")
        return this
    }

val FomanticUIClasses.required : FomanticUIClasses
    get() {
        classes("required")
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

val FomanticUIClasses.fileSignature: FomanticUIClasses
    get(){
        classes("file signature")
        return this
    }

val FomanticUIClasses.userCheck: FomanticUIClasses
    get(){
        classes("user check")
        return this
    }

val FomanticUIClasses.certificate: FomanticUIClasses
    get(){
        classes("certificate")
        return this
    }

val FomanticUIClasses.calendarAlternate: FomanticUIClasses
    get(){
        classes("calendar alternate")
        return this
    }

val FomanticUIClasses.server: FomanticUIClasses
    get(){
        classes("server")
        return this
    }

val FomanticUIClasses.city: FomanticUIClasses
    get(){
        classes("city")
        return this
    }

fun FomanticUIClasses.active(isActive: Boolean):FomanticUIClasses{
    if(isActive){
        classes("active")
    }
    return this
}

fun FomanticUIClasses.visible(isVisible: Boolean):FomanticUIClasses{
    if(isVisible){
        classes("visible")
    }
    return this
}

fun FomanticUIClasses.checked(isChecked: Boolean) = withOptionalAttribute("checked", isChecked)



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