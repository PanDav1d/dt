package de.doctag.lib.model

import de.doctag.lib.loadPublicKey
import de.doctag.lib.publicKeyFingerprint
import de.doctag.lib.savePrivateKey
import de.doctag.lib.savePublicKey
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class PublicKeyVerificationResult(
    val message: String,
    val signature: String
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

data class PublicKeyVerification(
    var signatureOfPublicKeyEntry: String? = null,
    var signedByPublicKey: String? = null,
    var signedByParty: String? = null,
    var signedAt: String? = null,
    var signatureValidUntil: String? = null,
    var isAddressVerified: Boolean? = null,
    var isSigningDoctagInstanceVerified: Boolean? = null
)


open class BasePublicKeyEntry(
    open var publicKey : String? = null,
    open val verboseName: String? = null,
    open var created: String? = null,
    open var owner: Person = Person(),
    open var ownerAddress: Address = Address(),
    open var signingDoctagInstance: String? = null,
    open var verification: PublicKeyVerification? = null
) {
    fun verifySignature() : Boolean {

        val publicKey = verification?.signedByPublicKey?.let { loadPublicKey(it) } ?: return false

        return de.doctag.lib.verifySignature(
            publicKey,
            getSignatureMessage(),
            verification?.signatureOfPublicKeyEntry!!
        )
    }

    fun getSignatureMessage() : String{

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
            verification?.signatureValidUntil,
            if(verification?.isAddressVerified == true) "true" else "false",
            if(verification?.isSigningDoctagInstanceVerified == true) "true" else "false"
        )

        return cols.joinToString(";")
    }
}

data class PrivatePublicKeyPair(
    var _id: String? = null,
    var privateKey: String? = null,
    var fingerprint: String? = null,
    override var publicKey : String? = null,
    override var verboseName: String? = null,
    override var created: String? = null,
    override var owner: Person = Person(),
    override var ownerAddress: Address = Address(),
    override var signingDoctagInstance: String? = null,
    override var verification: PublicKeyVerification? = null
) : BasePublicKeyEntry(publicKey, verboseName, created, owner,ownerAddress,signingDoctagInstance,verification) {
    companion object {
        fun make(verboseName:String, signingDoctagInstance: String, ownerAddress: Address, owner: Person): PrivatePublicKeyPair {
            val key  = PrivatePublicKeyPair()
            key.verboseName = verboseName

            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(ECGenParameterSpec("secp256r1"), SecureRandom())

            val pair = keyGen.generateKeyPair()
            val priv = savePrivateKey(pair.private)
            val pub = savePublicKey(pair.public)

            key.fingerprint = publicKeyFingerprint(pair.public)
            key.privateKey = priv
            key.publicKey = pub
            key.created = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            key.signingDoctagInstance = signingDoctagInstance
            key.ownerAddress = ownerAddress
            key.owner = owner

            return key
        }
    }
}