package de.doctag.keysrv

import de.doctag.keysrv.model.PublicKeyEntry
import de.doctag.lib.loadPrivateKey
import de.doctag.lib.loadPublicKey
import de.doctag.lib.model.Address
import de.doctag.lib.model.Person
import de.doctag.lib.model.PrivatePublicKeyPair
import de.doctag.lib.publicKeyFingerprint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SignPublicKeyTest {

    @Test
    fun `Test that a public key entry is correctly signed and its signature can be verified`(){

        //
        // Given
        //

        val signingKey = PrivatePublicKeyPair.make("Strictly private! Nowhere to be seen", "keyserver.doctag.de", Address("Frank ENGLERT","Frank ENGLERT","ELISABETH-HATTEMER-STRAßE 4","Darmstadt","64289","DE"), Person("5e9759eb7c44946335a65387", "Frank","Englert","f.englert@gmail.com"))
        val ppkToSign = PrivatePublicKeyPair.make("Key of anybody", "127.0.0.1", Address("Frank ENGLERT","Frank ENGLERT","ELISABETH-HATTEMER-STRAßE 4","Darmstadt","64289","DE"), Person("5e9759eb7c44946335a65387", "Frank","Englert","f.englert@gmail.com"))
        val publicKeyEntry = PublicKeyEntry(
            null,
            ppkToSign.publicKey,
            publicKeyFingerprint(loadPublicKey(ppkToSign.publicKey)!!),
            ppkToSign.verboseName,
            ppkToSign.created,
            ppkToSign.owner.let{Person(it.userId, it.firstName, it.lastName, it.email, it.phone)},
            ppkToSign.ownerAddress.let { Address(it.name1, it.name2, it.street, it.city, it.zipCode, it.countryCode) },
            "fe.kd.doctag.de",
            null
        )

        //
        // When
        //
        val signedKeyEntry = publicKeyEntry.makeSignedCopy(signingKey.publicKey!!, loadPrivateKey(signingKey.privateKey)!!, "keyserver.doctag.de", false, true)
        Assertions.assertEquals(signedKeyEntry.verification!!.isAddressVerified, false)
        Assertions.assertEquals(signedKeyEntry.verification!!.isSigningDoctagInstanceVerified, true)
        Assertions.assertEquals(signedKeyEntry.verification!!.signedByParty, "keyserver.doctag.de")
        Assertions.assertEquals(signedKeyEntry.verifySignature(), true)
    }
}