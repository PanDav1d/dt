package de.doctag.docsrv.model

import de.doctag.lib.model.PrivatePublicKeyPair
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

object DbContext {

    private val client by lazy{ KMongo.createClient(Config.instance.dbConnection) }
    private val database by lazy{ client.getDatabase(Config.instance.dbName)}

    val users = database.getCollection<User>()
    val documents = database.getCollection<Document>()
    val files = database.getCollection<FileData>()
    val keys = database.getCollection<PrivatePublicKeyPair>()
}