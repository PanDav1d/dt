import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import de.doctag.docsrv.DocSrvConfig



data class DocSrvConfigImpl(override val dbConnection: String, override val dbName: String) : DocSrvConfig

class DocSrvHostingControllerConfig {
    val dbConnection = System.getenv("dbConnection") ?: "mongodb://localhost:27017/?serverSelectionTimeoutMS=5000&connectTimeoutMS=10000"

    val dbName = System.getenv("dbName") ?: "docsrv-hosting-controller"


    val docSrvDbNameTemplate = System.getenv("docSrvDbNameTemplate") ?: "docsrv-@hostname"

    val smtpServer = System.getenv("smtpServer") ?:"localhost"

    val smtpUser = System.getenv("smtpUser")

    val smtpPassword = System.getenv("smtpPassword")

    val fromAddress = System.getenv("fromAddress") ?: "root@localhost"

    val baseDomainName = System.getenv("baseDomainName") ?: "localhost"

    val caddyConfigDir = System.getenv("caddyConfigDir") ?: "/etc/caddy/vhosts"

    val docsrvAddress = System.getenv("docsrvAddress") ?: "127.0.0.1:16097"

    val adminMailAddress = System.getenv("adminMailAddress") ?: "root@localhost"

}

object Config {
    val instance  by lazy { DocSrvHostingControllerConfig() }
}