package de.doctag.docsrvHostingController

import kweb.plugins.fomanticUI.fomantic
import kotlin.random.Random

import de.doctag.docsrv.generatePasswordHash
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.docsrv.ui.centeredBox
import de.doctag.docsrv.ui.forms.userAddForm
import de.doctag.docsrv.ui.navigateTo
import kotlinx.coroutines.*
import kweb.ElementCreator
import kweb.WebBrowser
import kweb.h2
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save
import kweb.*
import java.io.File
import java.time.ZonedDateTime


enum class SetupSteps {
    ENTER_PERSONAL_DATA,
    PIN_CHECK,
    ENTER_DOMAIN_NAME,
    LOADING_PAGE,
    RESULT_PAGE
}

private fun defaultWorkflow() = Workflow(name = "Default", actions = listOf(
    WorkflowAction("Verlader", inputs = listOf()),
    WorkflowAction("Transporteur", inputs = listOf(WorkflowInput("FZG", "Zum Transport eingesetztes Fahrzeug", kind = WorkflowInputKind.TextInput))),
    WorkflowAction("Warenempfänger", inputs = listOf(
                WorkflowInput("Vorbehalte", "Abweichungen bei der Zustellung / Freitext", kind = WorkflowInputKind.TextInput),
                WorkflowInput("Schadensdoku", "Fotos von etwaigen Beschädigungen", kind = WorkflowInputKind.FileInput)
            )
        )
    )
)


fun WebBrowser.handleCreateInstance(content: ElementCreator<*>) {

    val status = KVar(SetupSteps.ENTER_PERSONAL_DATA)
    val userInstance = KVar<User?>(null)
    val instance = HostedInstance()

    content.centeredBox {
        render(status) { rStatus ->
            div().new {
                when (rStatus) {
                    SetupSteps.ENTER_DOMAIN_NAME -> {
                        h2().text("System einrichten")


                        setupDomainNameForm { domainName ->
                            instance.domainName = domainName
                            status.value = SetupSteps.LOADING_PAGE

                            DbContext.hostedInstances.save(instance)
                        }
                    }
                    SetupSteps.ENTER_PERSONAL_DATA -> {
                        h2().text("System einrichten")
                        h4().text("Admin-Benutzer anlegen")

                        userAddForm(User()) { user, passwd ->

                            user.passwordHash = generatePasswordHash(passwd)
                            user.created = ZonedDateTime.now()

                            userInstance.value = user

                            status.value = SetupSteps.PIN_CHECK
                        }
                    }
                    SetupSteps.PIN_CHECK -> {
                        h2().text("System einrichten")

                        val serverPin = Random.nextInt(1_000_000, 9_999_999).toString()

                        logger.info("Pin is $serverPin")
                        GlobalScope.launch {
                            withContext(Dispatchers.IO){
                                sendServerPinMail(userInstance.value!!, serverPin)
                            }
                        }

                        setupPinCheck(serverPin) {
                            status.value = SetupSteps.ENTER_DOMAIN_NAME
                        }
                    }
                    SetupSteps.LOADING_PAGE -> {
                        val setupState = KVar(listOf("Caddy Konfiguration schreiben"))

                        h2().text("Einrichtung wird durchgeführt")
                        div(fomantic.ui.centered.inline.active.loader).new {
                            div(fomantic.ui.text.loader).text("Bitte warten")
                        }
                        p().new {
                            render(setupState){actions ->
                                ul(attributes = mapOf("style" to "list-style-type: \"-\"")).new{
                                    actions.forEach{item ->
                                        li(attributes = mapOf("style" to "text-align: left")).text(item)
                                    }
                                }
                            }
                        }



                        val file = File(Config.instance.caddyConfigDir + File.separator + instance.domainName+".conf")

                        logger.info("Writing caddy config to ${file.absolutePath}")

                        file.writeText("""
                            ${instance.domainName} {
                               reverse_proxy ${Config.instance.docsrvAddress}
                            }
                        """.trimIndent())

                        logger.info("Reloading caddy")
                        setupState.value = setupState.value.plus("Caddy Server neu starten")
                        shellExec("sudo service caddy reload")


                        logger.info("Saving setup to db")


                        setupState.value = setupState.value.plus("Datenbank erzeugen")
                        db(instance.domainName!!).users.save(userInstance.value!!)

                        val wf = defaultWorkflow()
                        wf.apply {
                            db(instance.domainName!!).workflows.save(wf)
                        }
                        db(instance.domainName!!).config.save(DocsrvConfig(_id = "1", hostname = instance.domainName!!, workflow = WorkflowConfig(defaultWorkflowId = wf._id)))


                        DbContext.hostedInstances.save(instance)


                        GlobalScope.launch {
                            withContext(Dispatchers.IO){
                                sendSetupCompletedMail(userInstance.value!!, "https://${instance.domainName}/")
                            }
                        }

                        GlobalScope.launch {
                            delay(5000)

                            val numRetries = 5
                            for(i in 0..numRetries) {
                                setupState.value = setupState.value.plus("Prüfen ob die Instanz erreichar ist. Versuch ${i+1} von ${numRetries+1}")
                                val isReachable = DocServerClient.checkHealth(instance.domainName!!)

                                if(isReachable) {
                                    setupState.value = setupState.value.plus("Instanz ist erreichbar")
                                    status.value = SetupSteps.RESULT_PAGE
                                }

                                delay(5000)
                            }
                        }
                    }

                    SetupSteps.RESULT_PAGE -> {
                        h2().text("Einrichtung abgeschlossen")
                        span().text("Das System wurde erfolgreich eingerichtet. Weiter zur ")
                        a(href = "https://${instance.domainName}/login").text("Anmeldung")

                        p().text("Sie werden automatisch in 5 Sekunden weitergeleitet.")
                        GlobalScope.launch {
                            delay(5000)
                            browser.navigateTo("https://${instance.domainName}/login")
                        }
                    }
                }
            }
        }
    }
}
