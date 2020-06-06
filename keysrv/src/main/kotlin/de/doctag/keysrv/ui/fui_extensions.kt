package de.doctag.keysrv.ui

import kweb.classes
import kweb.plugins.fomanticUI.FomanticUIClasses

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

