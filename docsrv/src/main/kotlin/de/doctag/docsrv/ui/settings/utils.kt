package de.doctag.docsrv.ui.settings

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
        a(fomantic.ui.item.active(activeItem == SettingsTabMenuActiveItem.User), "/settings/users").text("Benutzer")
        a(fomantic.ui.item.active(activeItem == SettingsTabMenuActiveItem.Keys), "/settings/keys").text("Schlüssel")
        a(fomantic.ui.item.active(activeItem == SettingsTabMenuActiveItem.System), "/settings/system").text("System")


        div(fomantic.right.menu).new {
            div(fomantic.item).new {
                rightAction()
            }
        }
    }
}