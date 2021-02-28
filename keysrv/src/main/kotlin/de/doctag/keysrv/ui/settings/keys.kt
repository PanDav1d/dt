package de.doctag.keysrv.ui.settings

import de.doctag.keysrv.model.DbContext
import de.doctag.keysrv.model.PublicKeyEntry
import de.doctag.keysrv.model.authRequired
import de.doctag.keysrv.ui.*
import de.doctag.keysrv.ui.modals.editKeyModal
import de.doctag.keysrv.ui.modals.editUserModal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save
import java.time.format.DateTimeFormatter

fun ElementCreator<*>.handleKeySettings(){
    authRequired {

        val keys = KVar(listOf<PublicKeyEntry>())

        fun loadDocuments(){
            keys.value = DbContext.publicKeys.find().toList()
        }
        loadDocuments()
        
        pageBorderAndTitle("Öffentliche Schlüssel") { pageArea ->

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
                                th().text("Instanz / Fingerprint")
                                th().text("Anzeigename")
                                th().text("Erstellt am")
                                th().text("Validiert am")
                                th().text("Von")
                                th().text("Instanz validiert")
                                th().text("Addresse validiert")
                                th().text("Besitzer Vorname")
                                th().text("Besitzer Nachname")
                                th().text("Org Name 1")
                                th().text("Org Name 2")
                                th().text("Org Straße")
                                th().text("Org PLZ")
                                th().text("Org Ort")
                                th().text("Org Land")
                                th().text("Aktion")
                            }
                        }
                        tbody().new {
                            rKeys.forEach { key ->

                                tr().new {
                                    td().text("${key.signingDoctagInstance}/${key.fingerpint}")
                                    td().text(key.verboseName ?: "")
                                    td().text(key.created?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "")
                                    td().text(key.verification?.signedAt?.split("T")?.get(0) ?: "---")
                                    td().text(key.verification?.signedByParty ?: "---")
                                    td().text(if(key.verification?.isSigningDoctagInstanceVerified==true)"ja" else "nein" )
                                    td().text(if(key.verification?.isAddressVerified==true)"ja" else "nein" )
                                    td().text("${key.owner.firstName}")
                                    td().text("${key.owner.lastName}")
                                    td().text("${key.ownerAddress.name1}")
                                    td().text("${key.ownerAddress.name2}")
                                    td().text("${key.ownerAddress.street}")
                                    td().text("${key.ownerAddress.city}")
                                    td().text("${key.ownerAddress.zipCode}")
                                    td().text("${key.ownerAddress.countryCode}")
                                    td().new {
                                        i(fomantic.ui.edit.icon).on.click {
                                            logger.info("Editing key ${key.verboseName}")

                                            val editModal = editKeyModal(key) { pke ->
                                                DbContext.publicKeys.save(pke)

                                                DoctagKeyClient.pushVerificationToDoctagInstance(
                                                    pke.signingDoctagInstance!!,
                                                    pke.publicKey!!,
                                                    pke.verification!!
                                                )

                                                GlobalScope.launch {
                                                    delay(250)
                                                    loadDocuments()
                                                }
                                            }
                                            editModal.open()
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
}