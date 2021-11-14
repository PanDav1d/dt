package de.doctag.docsrv.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.doctag.docsrv.api.EmbeddedDocument
import de.doctag.lib.*
import de.doctag.lib.model.Address
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.model.PublicKeyVerification
import org.litote.kmongo.`in`
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Session(
    val sessionId: String,
    val expires: ZonedDateTime,
    val name: String? = null
)

data class DocsrvConfig(
        var _id: String? = "1",
        var hostname: String = "",
        var outboundMail: OutboundMailConfig? = null,
        var inboundMail: InboundMailConfig? = null,
        var design: DesignConfig? = null,
        var workflow: WorkflowConfig? = null,
        var security: SecurityConfig? = null
)

data class SecurityConfig(
    var acceptSignaturesByUnverifiedKeys: Boolean? = null
)

data class WorkflowConfig(
        var defaultWorkflowId: String? = null
)

data class DesignConfig(
        val headerColor: String?=null,
        val headerTitle: String?=null
)

data class OutboundMailConfig(
        val server: String? = null,
        val user: String? = null,
        val password: String? = null,
        val fromAddress: String? =null
)

enum class InboundMailProtocol {
    POP3,
    IMAP
}

data class InboundMailConfig(
        var shouldReceiveDocumentsViaMail: Boolean? = null,
        var server: String? = null,
        var protocol: InboundMailProtocol? = null,
        var user: String? = null,
        var password: String? = null
)

data class User(
    var _id: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var emailAdress: String? = null,
    var passwordHash: String? = null,
    var created: ZonedDateTime?= null,
    var sessions: List<Session>?=null
)

data class DocumentId(
        val fullUrl : String,
        val hostname: String,
        val id: String
) {
    companion object {
        fun isValid(input:String) : Boolean {
            return input.trim().startsWith("http") && input.contains("/d/")
        }

        fun parse(input:String): DocumentId {
            val rest = input.split("://")[1]
            val (hostname, docId) = rest.split("/d/")

            return DocumentId(input, hostname, docId)
        }
    }
}

enum class DocumentSignRequestStatus {
    REQUESTED,
    SIGNED,
    REJECTED
}

data class DocumentSignRequest(
        var _id: String? = null,
        var doctagUrl: String? = null,
        val role: String? = null,
        val requestingParty: Address? = null,
        val createdBy: DocumentSignRequestUser? = null,
        val timestamp: ZonedDateTime? = ZonedDateTime.now(),
        var signedTimestamp: ZonedDateTime? = null,
        var status: DocumentSignRequestStatus = DocumentSignRequestStatus.REQUESTED
)

data class DocumentSignRequestUser(
        val userId: String? = null,
        val userName: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document(
    var _id: String? = null,
    var url : String? = null,
    var isMirrored: Boolean? = null,
    var originalFileName: String? = null,
    var attachmentId: String? = null,
    var attachmentHash: String? = null,
    var signatures: List<Signature>? = null,
    var created: ZonedDateTime? = null,
    var mirrors: List<String>? = null,
    var workflow: Workflow? = null,
    var fullText: String? = null
) {
    @JsonIgnore
    fun getWorkflowStatus() : List<Pair<String, Signature?>>{
        return workflow?.actions?.mapNotNull { action ->
            val signature = this.signatures?.find {sig->
                sig.role == action.role
            }

            action.role?.let{it to signature}
        } ?: listOf()
    }

    fun makeSignature(ppk:PrivatePublicKeyPair, role:String?, inputs: List<WorkflowInputResult>?) : Signature {
        val prevSignatureHash = this.signatures?.calculateSignatureHash()
        return Signature.make(ppk, this.url, this.attachmentHash, role, inputs, prevSignatureHash)
    }

    fun toEmbeddedDocument(db: DbContext): EmbeddedDocument {
        val fileIdList = listOf(this.attachmentId) + (this.signatures?.flatMap { it.inputs ?:listOf() }?.map { it.fileId } ?: listOf())
        val files = db.files.find(FileData::_id `in` fileIdList).toList()

        return EmbeddedDocument(files, this)
    }
}

data class FileData(
    var _id: String? = null,
    var name: String? = null,
    var base64Content: String? = null,
    var contentType: String? = null
)

data class Workflow(
        var _id: String? = null,
        var name: String? = null,
        var actions: List<WorkflowAction>? = null
) {
    fun modifyWorkflowActionWithIndex(idx: Int, modifyFunc: (WorkflowAction)->WorkflowAction) : Workflow {
        return this.copy(actions = this.actions?.mapIndexed { index, workflowAction ->
            if(index == idx){
                modifyFunc(workflowAction)
            }else {
                workflowAction
            }
        }?.toList())
    }
}

data class WorkflowAction(
        var role: String? = null,
        var inputs: List<WorkflowInput>? = null
)

data class WorkflowInput(
        var name: String? = null,
        var description: String? = null,
        var kind: WorkflowInputKind? = null,
        var options: WorkflowInputOptions? = null
)

data class WorkflowInputOptions(
    var signInputOptions: SignInputOptions? = null
)

data class SignInputOptions(
    var backgroundImage: String? = null
)

data class WorkflowInputResult(
        var name: String? = null,
        var value: String? = null,
        var fileId: String? = null
)

fun List<Signature>.calculateSignatureHash():String{
    if(this.isEmpty()){
        return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    }
    return this.map { it.data?.signature }.joinToString(",").toSha1HexString()
}

fun List<WorkflowInputResult>.calculateSha1Hash(role:String?)  : String {
    val dataToHash = (role?:"") + "\n" + this.map { "${it.name};${it.value};${it.fileId}" }.joinToString("\n")
    return dataToHash.toSha1HexString()
}

enum class WorkflowInputKind {
    TextInput,
    FileInput,
    SelectFromList,
    Checkbox,
    Sign
}

data class EmbeddedSignature(
        var files: List<FileData>,
        var signature: Signature
) {
    fun serialize():String{
        return getJackson().writeValueAsString(this)
    }

    companion object{
        fun load(input:String):EmbeddedSignature{
            return getJackson().readValue(input, EmbeddedSignature::class.java)
        }
    }
}

data class Signature(
    var data : DoctagSignatureData? = null,
    var signedByKey: PublicKeyResponse? = null,
    var signed: ZonedDateTime? = null,
    var originalMessage: String? = null,
    var role: String? = null,
    var inputs: List<WorkflowInputResult>? = null
) {
    @JsonIgnore
    fun isValid(): Boolean {

        val msg = this.originalMessage ?: return false
        val sig = this.data?.signature ?: return false
        val pk = signedByKey?.publicKey?.let {
            loadPublicKey(it)
        } ?: return false

        val currentSig = DoctagSignatureData.fromCsv(msg.split(";"))

        if(currentSig.workflowHash != inputs?.calculateSha1Hash(this.role) ?: ""){
            logger.info("Workflow hash does not match! Signature is not valid")
            return false
        }

        if(!this.data!!.validFromDateTime.isBefore(this.signed!!.plusMinutes(1)) || !this.data!!.validFromDateTime.isAfter(this.signed!!.minusMinutes(1))) {
            kweb.logger.error("Signature date does not match. Expected: ${this.signed!!.format(DateTimeFormatter.ISO_DATE_TIME)} != ${this.data?.validFromDateTime!!.format(DateTimeFormatter.ISO_DATE_TIME)}")
            return false
        }

        logger.info("V  SigMessage: ?;${msg.substringAfter(";")}")

        return verifySignature(pk, "?;"+msg.substringAfter(";"), sig)
    }

    companion object {
        fun make(currentKey: PrivatePublicKeyPair, documentUrl: String?, documentHash:String?, role:String?, result: List<WorkflowInputResult>?, previousSignatureHash: String?) : Signature{

            val workflowHash = result?.calculateSha1Hash(role )
            val sig = DoctagSignatureData.makeWithPPK(
                    currentKey,
                    Duration.ofSeconds(60),
                    documentUrl,
                    documentHash,
                    workflowHash,
                    previousSignatureHash
            )

            return Signature(
                    sig,
                    PublicKeyResponse(
                            currentKey.publicKey,
                            currentKey.verboseName,
                            currentKey.created,
                            currentKey.owner,
                            currentKey.ownerAddress,
                            currentKey.signingDoctagInstance,
                        currentKey.verification?.let {
                            PublicKeyVerification(
                                it.signatureOfPublicKeyEntry,
                                it.signedByPublicKey,
                                it.signedByParty,
                                it.signedAt,
                                it.signatureValidUntil,
                                it.isAddressVerified,
                                it.isSigningDoctagInstanceVerified
                            )
                        }
                    ),
                    ZonedDateTime.now(),
                    sig.toDataString(),
                    role,
                    result
            )
        }
    }
}
