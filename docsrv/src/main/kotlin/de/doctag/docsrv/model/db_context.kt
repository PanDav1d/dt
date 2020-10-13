package de.doctag.docsrv.model

import de.doctag.docsrv.Config
import de.doctag.lib.model.PrivatePublicKeyPair
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.request.host
import io.ktor.util.pipeline.PipelineContext
import kweb.Element
import kweb.ElementCreator
import org.litote.kmongo.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("de.doctag.docsrv.db_context");

class DbContext(dbName: String) {

    private val client by lazy{ KMongo.createClient(Config.instance.dbConnection) }
    private val database by lazy{ client.getDatabase(dbName)}

    val users = database.getCollection<User>()
    val documents = database.getCollection<Document>()
    val files = database.getCollection<FileData>()
    val keys = database.getCollection<PrivatePublicKeyPair>()
    val config = database.getCollection<DocsrvConfig>()
    val workflows = database.getCollection<Workflow>()
    val signRequests = database.getCollection<DocumentSignRequest>()

    val currentConfig by lazy {
        val configObj = config.findOne(DocsrvConfig::_id eq "1")
        if(configObj == null){
            val newConfig = DocsrvConfig(_id = "1")
            config.save(newConfig)
            newConfig
        }
        else {
            configObj
        }
    }
}

private val connections : MutableMap<String, DbContext> = mutableMapOf()
fun db(hostname:String) : DbContext  {
    val actualDbName = Config.instance.dbName.replace("@hostname", hostname).replace(".","_")

    return connections.getOrPut(actualDbName) {
        logger.info("Connecting to db $actualDbName")
        DbContext(actualDbName)
    }
}

fun PipelineContext<Unit, ApplicationCall>.db() = db(this.call.request.host())

fun ElementCreator<*>.db() = db(this.browser.host())
fun Element.db() = db(this.browser.host())