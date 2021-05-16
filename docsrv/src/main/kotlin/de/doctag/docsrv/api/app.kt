import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.route
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.throws
import de.doctag.docsrv.id
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
class UnauthorizedException : Exception()

fun NormalOpenAPIRoute.appApi2(){
    route("/app/auth_info") {
        throws(HttpStatusCode.Unauthorized, "User not authenticated", {ex: Exception -> ex.toString()}) {
            get<Unit, AuthInfoResponse>(id("fetchAuthInfo")) {
                val session = pipeline.call.request.cookies["SESSION"]
                val user = pipeline.db().users.findOne(User::sessions / Session::sessionId eq session)
                    ?: throw UnauthorizedException()

                respond(AuthInfoResponse(true, user.firstName, user.lastName))
            }
        }
    }
}
