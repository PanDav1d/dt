package de.doctag.docsrv.model

import de.doctag.docsrv.ui.centeredBox
import de.doctag.docsrv.ui.navigateTo
import io.ktor.features.callId
import io.ktor.features.origin
import io.ktor.request.host
import kweb.*
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.replaceOneById
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*


fun WebBrowser.getOrCreateSessionId() : String? {
    val sessionCookie = this.httpRequestInfo.cookies.get("SESSION")
    if(sessionCookie == null){
        val sessionId = UUID.randomUUID().toString()
        doc.cookie.setString("SESSION", sessionId, expires = Duration.ofDays(14))
        logger.info("SessionID ${sessionId}")
        return sessionId
    }
    logger.info("SessionID ${sessionCookie}")
    return sessionCookie
}

fun ElementCreator<*>.authRequired(block: ElementCreator<*>.()->Unit) {
    if(this.browser.authenticatedUser!=null){
        block()
    }
    else {
        if(this.browser.url.value != "/") {
            this.browser.navigateTo("/login?next=${this.browser.url.value}")
        }
        else {
            this.browser.navigateTo("/login")
        }
    }
}

val WebBrowser.authenticatedUser : User?
    get() = getOrCreateSessionId()?.let{Sessions.get(this,it)}

fun WebBrowser.clearSession() {
    val sessionId = getOrCreateSessionId()
    doc.cookie.set("SESSION", UUID.randomUUID().toString(), expires = Duration.ofDays(14))
    sessionId?.let{Sessions.remove(this, sessionId)}

}

fun WebBrowser.host()  = this.httpRequestInfo.requestedUrl.substringAfter("://").substringBefore("/").substringBefore(":")

object Sessions {

    fun start(browser: WebBrowser, sessionId: String, user: User){
        openSessions[sessionId] = user
        user.sessions = (user.sessions?:listOf()).plus(Session(sessionId, ZonedDateTime.now().plusDays(14)))
        db(browser.host()).users.replaceOneById(user._id!!, user)

    }

    fun get(browser: WebBrowser, sessionId: String) : User? {
        if(openSessions[sessionId]!=null)
            return openSessions[sessionId]
        val user = db(browser.host()).users.findOne(User::sessions/Session::sessionId eq sessionId)

        if(user!=null){
            openSessions[sessionId] = user
        }

        return user
    }

    fun remove(browser:WebBrowser, sessionId: String){
        this.openSessions.remove(sessionId)

        val users = db(browser.host()).users.find(User::sessions/Session::sessionId eq sessionId)
    }
    private val openSessions = mutableMapOf<String, User>()
}