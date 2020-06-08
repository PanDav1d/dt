package de.doctag.docsrvHostingController

import java.io.BufferedReader
import java.io.InputStreamReader


fun shellExec(command:String){
    val r = Runtime.getRuntime()
    val p = r.exec("uname -a")
    p.waitFor()
    val b = BufferedReader(InputStreamReader(p.inputStream))
    var line: String? = ""

    while (b.readLine().also { line = it } != null) {
        println(line)
    }

    b.close()
}