package de.doctag.docsrv.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.doctag.lib.PublicKeyResponse
import de.doctag.lib.DoctagSignature
import java.time.ZonedDateTime
import kotlin.random.Random

data class Session(
    val sessionId:String,
    val expires: ZonedDateTime
)

data class DocsrvConfig(
        var _id: String? = "1",
        var hostname: String = "",
        var outboundMail: OutboundMailConfig? = null,
        var inboundMail: InboundMailConfig? = null
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document(
    var _id: String? = null,
    var externalId: String? = null,
    var url : String? = null,
    var originalFileName: String? = null,
    var classifier: String? = null,
    var attachmentId: String? = null,
    var signatures: List<Signature>? = null,
    var created: ZonedDateTime? = null
)

data class FileData(
    var _id: String? = null,
    var name: String? = null,
    var base64Content: String? = null,
    val contentType: String? = null
)



data class Signature(
        var doc : DoctagSignature,
        var publicKey: PublicKeyResponse,
        var signed: ZonedDateTime,
        var originalMessage: String? = null
)
