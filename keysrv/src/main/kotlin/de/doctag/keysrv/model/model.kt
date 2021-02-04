package de.doctag.keysrv.model

import de.doctag.lib.loadPrivateKey
import de.doctag.lib.loadPublicKey
import de.doctag.lib.makeSignature
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.verifySignature
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
    var publicKey : String? = null,
    var fingerpint: String? = null,
    val verboseName: String? = null,
    var created: ZonedDateTime? = null,
    var owner: Person = Person(),
    var ownerAddress: Address = Address(),
    var signingDoctagInstance: String? = null,
    var verification: PublicKeyEntryVerification? = null
) {
    private fun sign(privateKey: PrivateKey) : String{
        val signature = makeSignature(privateKey, toCsv())
        return signature
    }

    fun makeSignedCopy(ppk: PrivatePublicKeyPair, signedByParty: String, addressVerified : Boolean, doctagInstanceVerified: Boolean ): PublicKeyEntry {
        val preSignedCopy = this.copy(verification = PublicKeyEntryVerification(
            signedByPublicKey = ppk.publicKey,
            signedAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            signedByParty = signedByParty,
            isAddressVerified = addressVerified,
            isSigningDoctagInstanceVerified = doctagInstanceVerified
        ))

        val signature = preSignedCopy.sign(loadPrivateKey(ppk.privateKey)!!)
        return preSignedCopy.copy(verification = preSignedCopy.verification!!.copy(
            signatureOfPublicKeyEntry = signature
        ))
    }

    fun verfiySignature() : Boolean {
        return verifySignature(loadPublicKey(verification?.signedByPublicKey!!)!!, toCsv(), verification?.signatureOfPublicKeyEntry!!)
    }

    fun toCsv() : String{

        val cols = listOf(
            publicKey,
            verboseName,
            owner.firstName,
            owner.lastName,
            owner.email,
            owner.phone,
            owner.userId,
            ownerAddress.name1,
            ownerAddress.name2,
            ownerAddress.city,
            ownerAddress.street,
            ownerAddress.zipCode,
            ownerAddress.countryCode,
            verification?.signedByPublicKey,
            verification?.signedByParty,
            verification?.signedAt,
            if(verification?.isAddressVerified == true) "true" else "false",
            if(verification?.isSigningDoctagInstanceVerified == true) "true" else "false"
        )

        return cols.joinToString(";")
    }
}



data class PublicKeyEntryVerification(
    var signatureOfPublicKeyEntry: String? = null,
    var signedByPublicKey: String? = null,
    var signedByParty: String? = null,
    var signedAt: String? = null,
    var isAddressVerified: Boolean? = null,
    var isSigningDoctagInstanceVerified: Boolean? = null
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