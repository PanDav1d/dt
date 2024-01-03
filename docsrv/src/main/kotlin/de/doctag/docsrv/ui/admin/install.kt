package de.doctag.docsrv.ui.admin

import de.doctag.docsrv.generatePasswordHash
import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.DocsrvConfig
import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
import de.doctag.docsrv.model.host
import de.doctag.docsrv.ui.centeredBox
import de.doctag.docsrv.ui.forms.userAddForm
import de.doctag.docsrv.ui.navigateTo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.ElementCreator
import kweb.WebBrowser
import kweb.h2
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save
import java.io.File
import kotlin.random.Random
import kweb.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.time.ZonedDateTime


enum class SetupSteps {
    PIN_CHECK,
    ENTER_DOMAIN_NAME,
    ENTER_PERSONAL_DATA,
    RESULT_PAGE
}

fun WebBrowser.handleInstall(content: ElementCreator<*>) {

    val status = KVar(SetupSteps.PIN_CHECK)

    if(db(host()).config.findOne(DocsrvConfig::_id eq "1")!=null){
        content.centeredBox {
            h2().text(i18n("ui.admin.install.doneHeader", "Einrichtung abgeschlossen"))
            p().text(i18n("ui.admin.install.doneText","Die Einrichtung Ihres Systems wurde bereits durchgeführt. Sie können den Docserver jetzt verwenden."))
        }
    }
    else {
        content.centeredBox {
            render(status) { rStatus ->
                div().new {
                    when (rStatus) {
                        SetupSteps.PIN_CHECK -> {
                            h2().text(i18n("ui.admin.install.pinCheckHeader","System einrichten"))

                            val serverPin = Random.nextInt(1_000_000, 9_999_999).toString()

                            val path = System.getProperty("user.home") + File.separator + "installPin_${host()}.txt"
                            val temp: File = File(path)
                            temp.writeText(serverPin + System.lineSeparator())

                            setupPinCheck(serverPin, path) {
                                status.value = SetupSteps.ENTER_DOMAIN_NAME
                            }
                        }
                        SetupSteps.ENTER_DOMAIN_NAME -> {
                            h2().text(i18n("ui.admin.install.domainNameHeder","System einrichten"))


                            setupDomainNameForm { domainName ->
                                db().config.save(DocsrvConfig(_id = "1", hostname = domainName))
                                status.value = SetupSteps.ENTER_PERSONAL_DATA
                            }
                        }
                        SetupSteps.ENTER_PERSONAL_DATA -> {
                            h2().text(i18n("ui.admin.install.personalDataHeader", "System einrichten"))
                            h4().text(i18n("ui.admin.install.createAdminUser","Admin-Benutzer anlegen"))

                            userAddForm(User(isAdmin = true)) { user, passwd ->

                                user.passwordHash = generatePasswordHash(passwd)
                                user.created = ZonedDateTime.now()
                                db().users.save(user)

                                status.value = SetupSteps.RESULT_PAGE
                            }
                        }
                        SetupSteps.RESULT_PAGE -> {
                            h2().i18nText("ui.admin.install.completedHeader","Einrichtung abgeschlossen")
                            span().i18nText("ui.admin.install.completedText","Das System wurde erfolgreich eingerichtet. Weiter zur ")
                            a(href = "/login").i18nText("ui.admin.install.loginLinkText", " Anmeldung")

                            p().i18nText("ui.admin.install.redirectText", "Sie werden automatisch in 5 Sekunden weitergeleitet.")
                            GlobalScope.launch {
                                delay(5000)
                                browser.navigateTo("/login")
                            }
                        }
                    }
                }
            }
        }
    }
}
