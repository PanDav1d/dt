package de.doctag.lib.model

import de.doctag.lib.savePrivateKey
import de.doctag.lib.savePublicKey
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.time.ZonedDateTime

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
    var hashOfPublicKeyEntry: String? = null,
    var signedByPublicKey: String? = null,
    var signedByParty: String? = null,
    var signedAt: ZonedDateTime? = null,
    var isAddressVerified: Boolean? = null
)

data class PrivatePublicKeyPair(
    var _id: String? = null,
    var privateKey: String? = null,
    var publicKey : String? = null,
    var verboseName: String? = null,
    var created: ZonedDateTime? = null,
    var owner: Person = Person(),
    var ownerAddress: Address = Address(),
    var signingDoctagInstance: String? = null,
    var verification: PublicKeyVerification? = null
) {
    companion object {
        fun make(verboseName:String, signingDoctagInstance: String, ownerAddress: Address, owner: Person): PrivatePublicKeyPair {
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
            key.signingDoctagInstance = signingDoctagInstance
            key.ownerAddress = ownerAddress
            key.owner = owner

            return key
        }
    }
}