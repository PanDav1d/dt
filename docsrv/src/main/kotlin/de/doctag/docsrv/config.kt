package de.doctag.docsrv

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

interface DocSrvConfig{
    val dbConnection: String
    val dbName: String
}

class DocsrvArgs(parser: ArgParser) : DocSrvConfig{
    override val dbConnection by parser.storing(
        "-c", "--dbConnection",
        help = "Database connection string to use"
    ).default("mongodb://localhost:27017/?serverSelectionTimeoutMS=5000&connectTimeoutMS=10000")

    override val dbName by parser.storing(
        "-d", "--dbName",
        help = "Name of the database").default("docsrv")


}

object Config {
    lateinit var _instance: DocSrvConfig
    val instance: DocSrvConfig
        get()=this._instance
}