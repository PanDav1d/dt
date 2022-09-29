import de.doctag.docsrv.api.*
import de.doctag.docsrv.checkPasswordHash
import de.doctag.docsrv.model.*
import de.doctag.docsrv.remotes.DocServerClient
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.toSha1HexString
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktor.swagger.get
import ktor.swagger.post
import ktor.swagger.ok
import ktor.swagger.operationId
import ktor.swagger.responds
import ktor.swagger.version.shared.Group
import org.litote.kmongo.*
import java.time.ZonedDateTime
import java.util.*


data class AuthInfoResponse(val authenticated: Boolean, val firstName: String?, val lastName: String?)
class UnauthorizedException : Exception()

data class SignatureInputs(
    val role: String,
    val ppkId: String,
    val inputs: List<WorkflowInputResult>?,
    val files: List<FileData>?
)

data class SignatureResult(
    val success: Boolean
)


fun PipelineContext<Unit, ApplicationCall>.ensureUserIsAuthenticated() : User {
    val session = this.call.request.cookies["SESSION"]
    if(session != null) {
        val user = this.db().users.findOne(User::sessions / Session::sessionId eq session)
            ?: throw UnauthorizedException()

        val userSession = user.sessions?.find { it.sessionId == session }

        if (userSession?.expires?.isAfter(ZonedDateTime.now()) != true)
            throw UnauthorizedException()

        return user
    }

    val basicAuthHeader = this.call.request.header("Authorization")
    if(basicAuthHeader != null && basicAuthHeader.startsWith("Basic")){
        val userPassB64 = basicAuthHeader.removePrefix("Basic").trim()
        val userPass = Base64.getDecoder().decode(userPassB64).toString(charset = Charsets.UTF_8)
        if(userPass.contains(":")){
            val (mail, pass) = userPass.split(":")

            val user = this.db().users.findOne(User::emailAdress eq mail)
            if(user != null &&  checkPasswordHash(user.passwordHash, pass)){
                return user
            }
        }
    }

    throw UnauthorizedException()
}

inline fun <reified T> T?.ensureObjectWasFound() : T{
    if(this == null){
        throw NotFoundException("Object was not found")
    }
    return this
}

fun Routing.appRoutes(){
    @Group("App")
    @Location("/app/auth_info")
    class AuthInfoRequestPath
    get<AuthInfoRequestPath>(
        "Check authentication".responds(
            ok<AuthInfoResponse>()
        ).operationId("fetchAuthInfo")
    ) { req ->
        val user = ensureUserIsAuthenticated()
        call.respond(HttpStatusCode.OK, AuthInfoResponse(true, user.firstName, user.lastName))
    }

    @Group("App")
    @Location("/app/signature/prepare/{documentId}/{hostname}")
    class FetchWorkflowToSignRequestPath(val documentId: String, val hostname: String)
    get<FetchWorkflowToSignRequestPath>(
        "Check authentication".responds(
            ok<PreparedSignature>()
        ).operationId("fetchWorkflowToSign")
    ) { req ->
        ensureUserIsAuthenticated()

        val docId = req.documentId
        val hostname = req.hostname

        val doc = withContext(Dispatchers.IO){
            DocServerClient.loadDocument("https://${hostname}/d/${docId}")
        }

        call.respond(
            PreparedSignature(
                doc?.document?.workflow,
                db().keys.find().map {
                    PrivatePublicKeyInfo(it._id!!, it.verboseName!!)
                }.toList()
            )
        )
    }

    @Group("App")
    @Location("/app/signature/push/{documentId}/{hostname}")
    class UploadWorkflowResultRequestPath(val documentId: String, val hostname: String)
    post<UploadWorkflowResultRequestPath, SignatureInputs>(
        "Check authentication".responds(
            ok<AuthInfoResponse>()
        ).operationId("uploadWorkflowResultAndTriggerSignature")
    ) { req, data ->
        val user = ensureUserIsAuthenticated()

        val doc = withContext(Dispatchers.IO){
            DocServerClient.loadDocument("https://${req.hostname}/d/${req.documentId}").ensureObjectWasFound()
        }

        val filesToInsert = data.files?.map {
            val input = data.inputs?.find { input -> input.fileId == it._id }
            it._id = it.base64Content!!.toSha1HexString()
            if(input != null) {
                input.fileId = it._id
            }
            it
        }
        filesToInsert?.let{
            if(filesToInsert.isNotEmpty()){
                db().files.insertMany(filesToInsert)
            }
        }

        val ppk = db().keys.findOne(PrivatePublicKeyPair::_id eq data.ppkId).ensureObjectWasFound()

        val addSignature = doc.document.makeSignature(ppk, data.role, data.inputs, "${user.firstName} ${user.lastName}")
        doc.document.signatures = (doc.document.signatures ?:listOf()) + addSignature

        val files = addSignature.inputs?.mapNotNull { it.fileId }?.distinct()?.mapNotNull { db().files.findOneById(it) }
        val embeddedSignature = EmbeddedSignature(files ?: listOf(), addSignature)

        withContext(Dispatchers.IO){
            DocServerClient.pushSignature(doc.document.url!!, embeddedSignature)
        }

        call.respond(SignatureResult(success = true))
    }
}
