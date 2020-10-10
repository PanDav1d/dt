package de.doctag.docsrv.ui.modals

import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.ui.*
import de.doctag.lib.*
import de.doctag.lib.model.PrivatePublicKeyPair
import kweb.*
import kweb.logger
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.property
import kweb.state.render
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import java.time.Duration
import java.time.ZonedDateTime

fun ElementCreator<*>.signDocumentModal(doc: Document, onSignFunc:(doc:Document)->Unit) = modal("Dokument signieren", autoFocus = false) { modal ->

    val role = KVar<WorkflowAction?>(null)
    val key = KVar<PrivatePublicKeyPair?>(null)
    var workflowResults: List<KVar<WorkflowInputResult>>?=null
    doc.workflow?.actions?.map { it.role }
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

        val keyOptions = db().keys.find().map { it._id to (it.verboseName ?:"")}.toMap()
        div(fomantic.ui.field).new {
            label().text("Schlüssel wählen")
            dropdown(keyOptions).onSelect { selectedKeyId ->
                key.value = db().keys.findOneById(selectedKeyId!!)
            }
        }

        h4(fomantic.ui.header.divider.horizontal).text("Zusatzdaten")

        render(role){rRole ->
            workflowResults = rRole?.inputs?.map { input->

                val result = KVar(WorkflowInputResult(name=input.name))

                when(input.kind){
                    WorkflowInputKind.TextInput->{
                        div(fomantic.ui.field).new {
                            label().text(input.name ?:"")
                            formInput(null, "", true, result.propertyOrDefault(WorkflowInputResult::value,""))
                                    .with(formCtrl)
                        }
                    }
                    WorkflowInputKind.Checkbox -> {
                        val checkedState = KVar(false)
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

                    }
                }

                result
            }
        }

        formSubmitButton(formCtrl, "Dokument signieren"){
            logger.info("Make signature for document ${doc.url}")

            val currentKey = key.value!!
            val sig = DoctagSignature.makeWithPPK(currentKey, Duration.ofSeconds(60), doc.url)
            val realResults = workflowResults?.map { it.value }

            val addSignature = Signature(sig, PublicKeyResponse(currentKey.publicKey, currentKey.verboseName, currentKey.owner, currentKey.issuer, currentKey.signingParty), ZonedDateTime.now(), null, role.value?.role, realResults)

            doc.signatures = (doc.signatures ?:listOf()) + addSignature

            db().documents.save(doc)
            onSignFunc(doc)
        }
    }
}
