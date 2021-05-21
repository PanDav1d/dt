import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.path.normal.route
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.throws
import de.doctag.docsrv.api.*
import de.doctag.docsrv.id
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.toSha1HexString
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById


data class AuthInfoResponse(val authenticated: Boolean, val firstName: String?, val lastName: String?)
class UnauthorizedException : Exception()

data class SignatureInputs(
    val role: String,
    val ppkId: String,
    val inputs: List<WorkflowInputResult>?,
    val files: List<FileData>
)

data class SignatureResult(
    val success: Boolean
)


@Path("{documentId}/{hostname}")
data class PrepareSignatureRequest(
    @PathParam("ID of the document to fetch") val documentId: String,
    @PathParam("Hostname of the document to fetch") val hostname: String
)

fun PipelineContext<Unit, ApplicationCall>.ensureUserIsAuthenticated() : User {
    val session = this.call.request.cookies["SESSION"]
    val user = this.db().users.findOne(User::sessions / Session::sessionId eq session)
        ?: throw UnauthorizedException()
    return user
}

inline fun <reified T> T?.ensureObjectWasFound() : T{
    if(this == null){
        throw NotFoundException("Object was not found")
    }
    return this
}

fun NormalOpenAPIRoute.appApi2(){
    route("/app/auth_info") {
        throws(HttpStatusCode.Unauthorized, "User not authenticated", {ex: Exception -> ex.toString()}) {
            get<Unit, AuthInfoResponse>(id("fetchAuthInfo")) {
                val user = pipeline.ensureUserIsAuthenticated()

                respond(AuthInfoResponse(true, user.firstName, user.lastName))
            }
        }
    }

    route("/app/signature/prepare"){
        throws(HttpStatusCode.Unauthorized, "User not authenticated", {ex: Exception -> ex.toString()}) {
            get<PrepareSignatureRequest, PreparedSignature>(id("fetchWorkflowToSign")) { params ->
                pipeline.ensureUserIsAuthenticated()

                val docId = params.documentId
                val hostname = params.hostname

                val doc = DocServerClient.loadDocument("https://${hostname}/d/${docId}")

                respond(
                    PreparedSignature(
                        doc?.document?.workflow!!,
                        pipeline.db().keys.find().map {
                            PrivatePublicKeyInfo(it._id!!, it.verboseName!!)
                        }.toList()
                    )
                )
            }
        }
    }

    route("/app/signature/push"){
        throws(HttpStatusCode.Unauthorized, "User not authenticated", {ex: Exception -> ex.toString()}) {
            post<PrepareSignatureRequest, SignatureResult, SignatureInputs>(id("fetchWorkflowToSign")) { req, data ->
                pipeline.ensureUserIsAuthenticated()

                val doc = DocServerClient.loadDocument("https://${req.hostname}/d/${req.documentId}").ensureObjectWasFound()

                val filesToInsert = data.files.map {
                    val input = data.inputs?.find { input -> input.fileId == it._id }
                    it._id = it.base64Content!!.toSha1HexString()
                    if(input != null) {
                        input.fileId = it._id
                    }
                    it
                }
                pipeline.db().files.insertMany(filesToInsert)

                val ppk = pipeline.db().keys.findOne(PrivatePublicKeyPair::_id eq data.ppkId).ensureObjectWasFound()

                val addSignature = doc.document.makeSignature(ppk, data.role, data.inputs)
                doc.document.signatures = (doc.document.signatures ?:listOf()) + addSignature

                val files = addSignature.inputs?.mapNotNull { it.fileId }?.distinct()?.mapNotNull { pipeline.db().files.findOneById(it) }
                val embeddedSignature = EmbeddedSignature(files ?: listOf(), addSignature)

                DocServerClient.pushSignature(doc.document.url!!, embeddedSignature)

                respond(SignatureResult(success = true))
            }
        }
    }
}
