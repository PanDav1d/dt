package de.docward.docsrv.model

import de.docward.docsrv.DocSignature
import de.docward.docsrv.keysrv_api.PublicKeyResponse
import de.docward.docsrv.savePrivateKey
import de.docward.docsrv.savePublicKey
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
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

data class PrivatePublicKeyPair(
    var privateKey: String? = null,
    var publicKey : String? = null,
    var verboseName: String? = null,
    var created: ZonedDateTime? = null,
    var owner: Person = Person(),
    var issuer: Address = Address(),
    var signingParty: String? = null
) {
    companion object {
        fun make(verboseName:String, signingParty: String, issuer: Address, owner: Person): PrivatePublicKeyPair {
            val key  = PrivatePublicKeyPair()
            key.verboseName = verboseName

            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())

            val pair = keyGen.generateKeyPair()
            val priv = savePrivateKey(pair.private)
            val pub = savePublicKey(pair.public)

            key.privateKey = priv
            key.publicKey = pub
            key.created = ZonedDateTime.now()
            key.signingParty = signingParty
            key.issuer = issuer
            key.owner = owner

            return key
        }
    }
}

data class Signature(
        var doc : DocSignature,
        var publicKey: PublicKeyResponse,
        var signed: ZonedDateTime,
        var originalMessage: String? = null
)

data class SigningParty(
        var company: Address? = null,
        var employee: Person? = null
)

data class Person(
        var userId: String? = null,
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var phone: String? = null
)

data class Address(
        var name1: String? = null,
        var name2: String? = null,
        var street: String? = null,
        var city: String? = null,
        var zipCode: String? = null,
        var countryCode: String? = null
)