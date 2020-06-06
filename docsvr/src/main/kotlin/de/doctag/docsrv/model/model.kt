package de.doctag.docsrv.model

import de.doctag.lib.PublicKeyResponse
import de.doctag.lib.DoctagSignature
import java.time.ZonedDateTime

data class Session(
    val sessionId:String,
    val expires: ZonedDateTime
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
