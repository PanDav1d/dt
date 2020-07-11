package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.model.authRequired
import de.doctag.docsrv.ui.pageBorderAndTitle
import kweb.*
import kweb.plugins.fomanticUI.fomantic

fun ElementCreator<*>.handleSystemSettings() {
    authRequired {
        pageBorderAndTitle("Einstellungen") { pageArea ->

            div(fomantic.content).new() {
                settingsTabMenu(SettingsTabMenuActiveItem.System) {}

                div(fomantic.ui.divider.hidden)

                div(fomantic.ui.grid).new {
                    div(fomantic.ui.four.wide.column).new {
                        p().text("menu")
                    }
                    div(fomantic.ui.twelve.wide.column).new {
                        p().text("content")
                    }
                }

            }

        }
    }
}