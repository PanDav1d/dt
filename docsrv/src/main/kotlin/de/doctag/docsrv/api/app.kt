import de.doctag.docsrv.model.Session
import de.doctag.docsrv.model.User
import de.doctag.docsrv.model.db
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne


data class AuthInfoResponse(val authenticated: Boolean, val firstName: String?, val lastName: String?)

fun Routing.appApi(){
    get("/app/auth_info"){ req ->
        val session = call.request.cookies["SESSION"]

        val user = db().users.findOne(User::sessions / Session::sessionId eq session)

        if(user != null) {
            call.respond(HttpStatusCode.OK, AuthInfoResponse(true, user.firstName, user.lastName))
        }
        else {
            call.respond(HttpStatusCode.Unauthorized, AuthInfoResponse(false, null, null))
        }
    }
}