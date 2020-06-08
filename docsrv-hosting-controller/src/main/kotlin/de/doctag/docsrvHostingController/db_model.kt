package de.doctag.docsrvHostingController

import de.doctag.docsrv.model.DocsrvConfig
import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData
import de.doctag.docsrv.model.User
import de.doctag.lib.model.PrivatePublicKeyPair
import org.litote.kmongo.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("de.doctag.docsrv.db_context");

object DbContext {

    private val client by lazy{ KMongo.createClient(Config.instance.dbConnection) }
    private val database by lazy{ client.getDatabase(Config.instance.dbName)}

    val hostedInstances = database.getCollection<HostedInstance>()

}

data class HostedInstance(
        val _id: String? = null,
        var domainName: String? = null,
        var setupUser: User? = null,
        var emailValid: Boolean? = null,
        var domainNameValid: Boolean? = null
)