package de.doctag.docsrv.model

import de.doctag.docsrv.ui.centeredBox
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
            this.browser.url.value = "/login?next=${this.browser.url.value}"
        }
        else {
            this.browser.url.value = "/login"
        }
    }
}

val WebBrowser.authenticatedUser : User?
    get() = getOrCreateSessionId()?.let{Sessions.get(it)}

fun WebBrowser.clearSession() {
    val sessionId = getOrCreateSessionId()
    doc.cookie.set("SESSION", UUID.randomUUID().toString(), expires = Duration.ofDays(14))
    sessionId?.let{Sessions.remove(sessionId)}

}

object Sessions {

    fun start(sessionId: String, user: User){
        openSessions[sessionId] = user
        user.sessions = (user.sessions?:listOf()).plus(Session(sessionId, ZonedDateTime.now().plusDays(14)))
        DbContext.users.replaceOneById(user._id!!, user)

    }

    fun get(sessionId: String) : User? {
        if(openSessions[sessionId]!=null)
            return openSessions[sessionId]
        val user = DbContext.users.findOne(User::sessions/Session::sessionId eq sessionId)

        if(user!=null){
            openSessions[sessionId] = user
        }

        return user
    }

    fun remove(sessionId: String){
        this.openSessions.remove(sessionId)

        val users = DbContext.users.find(User::sessions/Session::sessionId eq sessionId)
    }
    private val openSessions = mutableMapOf<String, User>()
}