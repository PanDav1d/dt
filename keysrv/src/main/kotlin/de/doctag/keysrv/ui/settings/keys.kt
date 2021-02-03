package de.doctag.keysrv.ui.settings

import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.authRequired
import de.doctag.keysrv.ui.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.time.format.DateTimeFormatter

fun ElementCreator<*>.handleKeySettings(){
    authRequired {

        val keys = KVar(DbContext.publicKeys.find().toList())

        
        pageBorderAndTitle("Öffentliche Schlüssel") {pageArea->

            div(fomantic.content).new() {
                div(fomantic.ui.secondary.pointing.menu).new{
                    a(fomantic.ui.item, "/settings/users").text("Benutzer")
                    a(fomantic.ui.item.active, "/settings/keys").text("Öffentliche Schlüssel")
                }

                div(fomantic.ui.divider.hidden)

                render(keys) { rKeys ->

                    logger.info("List of Keys did change")

                    table(fomantic.ui.selectable.celled.table).new {
                        thead().new {
                            tr().new {
                                th().text("ID")
                                th().text("Anzeigename")
                                th().text("Erstellt am")
                                th().text("von")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rKeys.forEach { key ->

                                tr().new {
                                    td().text("${key.signingDoctagInstance}/${key.fingerpint}")
                                    td().text(key.verboseName ?: "")
                                    td().text(key.created?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "")
                                    td().text("${key.owner?.firstName} ${key.owner?.lastName}")
                                    td().new {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}