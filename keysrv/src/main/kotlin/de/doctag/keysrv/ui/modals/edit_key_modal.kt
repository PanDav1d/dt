package de.doctag.keysrv.ui.modals

import de.doctag.keysrv.model.*
import de.doctag.keysrv.propertyOrDefault
import de.doctag.keysrv.ui.*
import de.doctag.lib.loadPrivateKey
import de.doctag.lib.model.PublicKeyVerification
import kotlinx.coroutines.delay
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import java.time.format.DateTimeFormatter


fun ElementCreator<*>.editKeyModal(pke: PublicKeyEntry, onEdit: (pke:PublicKeyEntry)->Unit) =
        modal("Schlüssel bearbeiten") { modal ->
            tab(
                TabPane("Übersicht") {
                    div(fomantic.ui.segment).new{
                        h3().text("Öffentlicher Schlüssel")
                        div(attributes = mapOf("style" to "font-family: monospace;overflow-wrap: break-word;")).text(pke.publicKey?:"---")
                    }
                },
                TabPane("Signatur"){

                    val signatureState = when{
                        pke.verification?.signatureOfPublicKeyEntry == null -> "nicht signiert"
                        pke.verifySignature() -> "gültig signiert"
                        else -> "ungültig signiert"
                    }

                    val addressState = when{
                        pke.verification?.isAddressVerified == true -> "Addresse gültig"
                        pke.verification?.isAddressVerified == false -> "Addresse nicht gültig"
                        else -> "Addresse nicht geprüft"
                    }

                    val doctagInstanceState = when{
                        pke.verification?.isSigningDoctagInstanceVerified == true -> "Instanz existiert"
                        pke.verification?.isSigningDoctagInstanceVerified == false -> "Instanz existiert nicht"
                        else -> "nicht geprüft"
                    }

                    div(fomantic.ui.list).new {
                        itemWithIcon(fomantic.ui.fileSignature.icon, signatureState, "Signaturstatus")

                        if(pke.verification?.signatureOfPublicKeyEntry != null){
                            itemWithIcon(fomantic.ui.userCheck.icon, pke.verification?.signedByParty?:"---", "hat signiert")
                            itemWithIcon(fomantic.ui.calendarAlternate.icon, pke.verification?.signedAt?.format(
                                DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "---", "am")
                            itemWithIcon(fomantic.ui.certificate.icon, pke.verification?.signatureOfPublicKeyEntry ?: "---", "Signatur")
                            itemWithIcon(fomantic.ui.city.icon, addressState, "Addressprüfung")
                            itemWithIcon(fomantic.ui.server.icon, doctagInstanceState, "DocTag Instanz Prüfung")
                        }
                    }

                    val keyVerification = KVar(pke.verification ?: PublicKeyVerification())
                    keyVerification.value.signedByParty = this.browser.authenticatedUser.let { "${it?.firstName} ${it?.lastName}" }

                    val privateKey = KVar("")

                    h3(fomantic.ui.dividing.header).text("Signieren")

                    formControl { formCtrl ->
                        formInput( "Signiert von", "",false, keyVerification.propertyOrDefault(PublicKeyVerification::signedByParty, ""))
                            .with(formCtrl)

                        formInput( "Öffentlicher Schlüssel", "",false, keyVerification.propertyOrDefault(PublicKeyVerification::signedByPublicKey, ""))
                            .with(formCtrl)

                        formInput( "Privater Schlüssel", "",false, privateKey)
                            .withInputMissingErrorMessage("Bitte geben Sie einen gültigen Privaten Schlüssel an")
                            .with(formCtrl)


                        loadingCheckBoxInput("DocTag Instanz geprüft", keyVerification.propertyOrDefault(PublicKeyVerification::isSigningDoctagInstanceVerified, false), errorText = "Überprüfung fehlgeschlagen"){
                            try {
                                DoctagKeyClient.verifiyDoctagInstanceHasPrivateKey(pke.signingDoctagInstance!!, pke.publicKey!!)
                            }
                            catch(ex:Exception){
                                logger.error("Failed to verifyDoctagInstanceHasPrivateKey. Reason ${ex}")
                                false
                            }
                        }

                        checkBoxInput("Addresse geprüft", keyVerification.propertyOrDefault(PublicKeyVerification::isAddressVerified, false))

                        displayErrorMessages(formCtrl)

                        formSubmitButton(formCtrl, "Signieren"){
                            logger.info("Signing public key ${pke.signingDoctagInstance}/${pke.fingerpint}")
                            try {
                                val publicKey = keyVerification.value.signedByPublicKey!!.trim()
                                val privKey = loadPrivateKey(privateKey.value.trim())


                                val signedCopy = pke.makeSignedCopy(
                                    publicKey,
                                    privKey,
                                    keyVerification.value.signedByParty!!,
                                    keyVerification.value.isAddressVerified ?: false,
                                    keyVerification.value.isSigningDoctagInstanceVerified ?: false
                                )

                                if (signedCopy.verifySignature()) {
                                    logger.info("Public key ${pke.signingDoctagInstance}/${pke.fingerpint} successfully signed")
                                    onEdit(signedCopy)
                                    modal.close()
                                } else {
                                    logger.error("Signing of Public key ${pke.signingDoctagInstance}/${pke.fingerpint} failed")
                                    formCtrl.errors.value =
                                        listOf("Signieren fehlgeschlagen. Bitte prüfen Sie den eingegebenen Schlüssel")
                                }
                            }
                            catch(ex:Exception){
                                logger.error("Failed to sign. Reason $ex")
                                formCtrl.errors.value =
                                    listOf("Signieren fehlgeschlagen. Bitte prüfen Sie den eingegebenen Schlüssel")
                            }
                        }
                    }
                }
            )
        }