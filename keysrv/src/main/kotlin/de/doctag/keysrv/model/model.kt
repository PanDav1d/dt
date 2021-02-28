package de.doctag.keysrv.model

import de.doctag.lib.makeSignature
import de.doctag.lib.model.Address
import de.doctag.lib.model.BasePublicKeyEntry
import de.doctag.lib.model.Person
import de.doctag.lib.model.PublicKeyVerification
import java.security.PrivateKey
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

data class PublicKeyEntry(
    var _id: String? = null,
    var fingerpint: String? = null,
    override var publicKey : String? = null,
    override var verboseName: String? = null,
    override var created: String? = null,
    override var owner: Person = Person(),
    override var ownerAddress: Address = Address(),
    override var signingDoctagInstance: String? = null,
    override var verification: PublicKeyVerification? = null
) : BasePublicKeyEntry(publicKey, verboseName, created, owner, ownerAddress, signingDoctagInstance, verification) {

    private fun sign(privateKey: PrivateKey) : String{
        val signature = makeSignature(privateKey, getSignatureMessage())
        return signature
    }

    fun makeSignedCopy(publicKey: String, privateKey: PrivateKey?, signedByParty: String, addressVerified : Boolean, doctagInstanceVerified: Boolean ): PublicKeyEntry {
        val preSignedCopy = this.copy(verification = PublicKeyVerification(
            signedByPublicKey = publicKey,
            signedAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            signatureValidUntil = ZonedDateTime.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
            signedByParty = signedByParty,
            isAddressVerified = addressVerified,
            isSigningDoctagInstanceVerified = doctagInstanceVerified
        ))

        val signature = preSignedCopy.sign(privateKey!!)
        return preSignedCopy.copy(verification = preSignedCopy.verification!!.copy(
            signatureOfPublicKeyEntry = signature
        ))
    }
}