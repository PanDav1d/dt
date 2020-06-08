package de.doctag.docsrvHostingController

import kweb.plugins.fomanticUI.fomantic
import kotlin.random.Random

import de.doctag.docsrv.generatePasswordHash
import de.doctag.docsrv.model.DocsrvConfig
import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
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

fun WebBrowser.handleCreateInstance(content: ElementCreator<*>) {

    val status = KVar(SetupSteps.ENTER_DOMAIN_NAME)
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
                        h2().text("Einrichtung wird durchgeführt")
                        div(fomantic.ui.centered.inline.loader).text("Bitte warten")

                        val file = File(Config.instance.caddyConfigDir + File.separator + instance.domainName+".conf")
                        file.writeText("""
                            ${instance.domainName} {
                               tls ${Config.instance.adminMailAddress}
                               proxy / ${Config.instance.docsrvAddress} {
                                 websocket
                                 transparent
                               }
                            }
                        """.trimIndent())

                        shellExec("sudo service caddy reload")

                        db(instance.domainName!!).users.save(userInstance.value!!)
                        db(instance.domainName!!).config.save(DocsrvConfig(_id = "1", hostname = instance.domainName!!))
                        DbContext.hostedInstances.save(instance)

                        GlobalScope.launch {
                            delay(5000)
                            status.value = SetupSteps.RESULT_PAGE
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
