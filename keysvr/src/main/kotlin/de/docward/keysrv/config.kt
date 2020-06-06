import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default


interface KeySrvConfig{
    val dbConnection: String
    val dbName: String
}

class KeySrvArgs(parser: ArgParser) : KeySrvConfig{
    override val dbConnection by parser.storing(
        "-c", "--dbConnection",
        help = "Database connection string to use"
    ).default("mongodb://localhost:27017/?serverSelectionTimeoutMS=5000&connectTimeoutMS=10000")

    override val dbName by parser.storing(
        "-d", "--dbName",
        help = "Name of the database").default("keysrv")
}

object Config {
    lateinit var _instance: KeySrvConfig
    val instance: KeySrvConfig
        get()=this._instance
}