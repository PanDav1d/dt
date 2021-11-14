package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.fromDataUrl
import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.*
import de.doctag.lib.*
import de.doctag.lib.model.PrivatePublicKeyPair
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.logger
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

fun ElementCreator<*>.signDocumentModal(doc: Document, onSignFunc:(doc:Document, addedSig: Signature)->Unit) = modal("Dokument signieren", autoFocus = false) { modal ->

    val role = KVar<WorkflowAction?>(null)
    val key = KVar<PrivatePublicKeyPair?>(null)
    var workflowResults: List<KVar<WorkflowInputResult>>?=null
    val filesToAdd = mutableListOf<FileData>()

    formControl { formCtrl->

        h4(fomantic.ui.header.divider.horizontal).text("Allgemeine Angaben").focus()

        input(type = InputType.hidden)

        val roleOptions : Map<String?, String>? = doc.workflow?.actions?.mapIndexed { index, workflowAction ->
            index.toString() to (workflowAction.role ?: "")
        }?.toMap()

        if(roleOptions != null) {
            div(fomantic.ui.field).new {
                label().text("Rolle wählen")
                dropdown(roleOptions).onSelect { selectedRoleIdx ->
                    role.value = doc.workflow?.actions!![selectedRoleIdx!!.toInt()]
                }
            }
        }
        formCtrl.withValidation {
            if(role.value == null) "Bitte wählen Sie eine Rolle aus" else null
        }

        val keyOptions = db().keys.find().map { it._id to (it.verboseName ?:"")}.toMap()
        div(fomantic.ui.field).new {
            label().text("Schlüssel wählen")
            dropdown(keyOptions).onSelect { selectedKeyId ->
                val currentKey = db().keys.findOne(PrivatePublicKeyPair::_id eq selectedKeyId)
                logger.info("Selected key: ${selectedKeyId}. key.value = ${currentKey?.verboseName}" )
                key.value = currentKey
            }
        }
        formCtrl.withValidation {
            if(key.value == null) "Bitte wählen Sie einen Schlüssel aus" else null
        }

        h4(fomantic.ui.header.divider.horizontal).text("Zusatzdaten")

        render(role){rRole ->
            logger.info("Rendering selected role ${rRole?.role}")
            workflowResults = rRole?.inputs?.map { input->

                val result = KVar(WorkflowInputResult(name=input.name))

                when(input.kind){
                    WorkflowInputKind.TextInput->{
                        div(fomantic.ui.field).new {
                            label().text(input.name ?:"")
                            span().text(input.description?:"")
                            formInput(null, "", true, result.propertyOrDefault(WorkflowInputResult::value,""))
                        }
                    }
                    WorkflowInputKind.Checkbox -> {
                        val checkedState = KVar(false)
                        checkedState.addListener { old, new ->
                            result.value.value = new.toString()
                        }
                        div(fomantic.ui.field).new {
                            label().text(input.name ?: "")
                            checkBoxInput(
                                    input.description ?: input.name ?: "",
                                    checkedState
                            )
                        }
                    }
                    WorkflowInputKind.SelectFromList -> {

                    }
                    WorkflowInputKind.FileInput -> {
                        div(fomantic.ui.field).new {
                            label().text(input.name ?: "")
                            span().text(input.description?:"")
                            val fileField = fileInput(null, "", false, KVar(""))
                            fileField.onFileSelect {
                                fileField.retrieveFile { fd ->
                                    logger.info("Received file ${fd.fileName}")
                                    val (contentType, data) = fd.base64Content.removePrefix("data:").split(";base64,")

                                    val fileObj = FileData(_id = data.toSha1HexString(), name = fd.fileName, base64Content = data, contentType = contentType)
                                    fileObj.apply {
                                        db().files.save(fileObj)
                                    }
                                    filesToAdd.add(fileObj)
                                    result.value.fileId = fileObj._id
                                }
                            }
                        }
                    }
                    WorkflowInputKind.Sign -> {
                        div(fomantic.ui.field).new {
                            label().text(input.name ?: "")
                            p().text(input.description ?: "")
                            val sigPad = inputSignatureElement(backgroundImage = input.options?.signInputOptions?.backgroundImage)

                            /*
                            formCtrl.withValidation {
                                when{
                                    sigPad.isEmpty().get()==true-> "Die Unterschrift wird benötigt!"
                                    else -> null
                                }
                            }*/

                            formCtrl.withSubmitAction {
                                logger.info("Handling submit action")
                                val cf = CompletableFuture<Boolean>()
                                sigPad.fetchContent { pos ->
                                    val (contentType, data) = pos.base64Content.fromDataUrl()
                                    val fd = FileData(_id=data.toSha1HexString(), base64Content = data, contentType = contentType, name = "signature.png")
                                    logger.info("Received signature")

                                    fd.apply {
                                        db().files.save(fd)
                                    }
                                    filesToAdd.add(fd)
                                    result.value.fileId = fd._id

                                    logger.info("Stored signature: ok")
                                    cf.complete(true)
                                }
                                //cf.get()
                            }

                            GlobalScope.launch {
                                delay(100)
                                sigPad.onEndDraw { logger.info("Finished drawing") }
                            }
                        }
                    }
                }

                result
            }
        }

        displayErrorMessages(formCtrl)

        div(fomantic.ui.divider.hidden)

        formSubmitButton(formCtrl, "Dokument signieren"){
            logger.info("Make signature for document ${doc.url}")

            formCtrl.submit()

            val currentKey = key.value!!

            val realResults = workflowResults?.map { it.value }
            val addSignature = doc.makeSignature(currentKey, role.value?.role, realResults)

            doc.signatures = (doc.signatures ?:listOf()) + addSignature

            val files = addSignature.inputs?.mapNotNull { it.fileId }?.distinct()?.mapNotNull { db().files.findOneById(it) }
            val embeddedSignature = EmbeddedSignature(files ?: listOf(), addSignature)
            DocServerClient.pushSignature(doc.url!!, embeddedSignature)

            db().documents.save(doc)
            onSignFunc(doc, addSignature)
            modal.close()
        }
    }
}
