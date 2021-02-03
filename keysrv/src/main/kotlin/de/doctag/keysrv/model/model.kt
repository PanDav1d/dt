package de.doctag.keysrv.model

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
)

data class PublicKeyEntryVerification(
    var hashOfPublicKeyEntry: String? = null,
    var signedByPublicKey: String? = null,
    var signedByParty: String? = null,
    var signedAt: ZonedDateTime? = null,
    var isAddressVerified: Boolean? = null
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