package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.i18nText
import kweb.ElementCreator
import kweb.a
import kweb.div
import kweb.new
import kweb.plugins.fomanticUI.fomantic
import de.doctag.docsrv.ui.active
import kweb.state.KVar
import kweb.state.render

enum class SettingsTabMenuActiveItem {
    User,
    Keys,
    System
}


fun ElementCreator<*>.settingsTabMenu(activeItem: SettingsTabMenuActiveItem, rightAction: ElementCreator<*>.()->Unit)  {
    div(fomantic.ui.secondary.pointing.menu).new{
        a(fomantic.ui.item.active(activeItem == SettingsTabMenuActiveItem.User), "/settings/users").i18nText("ui.settings.utils.menu.user","Benutzer")
        a(fomantic.ui.item.active(activeItem == SettingsTabMenuActiveItem.Keys), "/settings/keys").i18nText("ui.settings.utils.menu.certificates","Teilnehmerzertifikate")
        a(fomantic.ui.item.active(activeItem == SettingsTabMenuActiveItem.System), "/settings/system").i18nText("ui.settings.utils.menu.system","System")


        div(fomantic.right.menu).new {
            div(fomantic.item).new {
                rightAction()
            }
        }
    }
}