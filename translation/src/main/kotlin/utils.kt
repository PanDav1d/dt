import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URLDecoder
import java.util.jar.JarFile

class Utils {

    companion object {
        private fun readResourceAsStream(pathInResourceFolder: String): InputStream? {
            return this::class.java.getResourceAsStream(pathInResourceFolder)
        }

        fun getResourceFiles(path: String): List<String> {

            val className = this::class.java.name.replace('.', '/')
            val classJar = this::class.java.getResource("/$className.class").toString()

            return if (classJar.startsWith("jar:")) {
                getResourceListing(this::class.java, path) ?: listOf()
            } else {
                readResourceAsStream(path).use {
                    return if (it == null) listOf()
                    else BufferedReader(InputStreamReader(it)).readLines().filter { it.isNotBlank() }
                }
            }
        }


        fun getResourceListing(clazz: Class<*>, path: String): List<String>? {
            var dirURL = clazz.classLoader.getResource(path)
            if (dirURL != null && dirURL.protocol.equals("file")) {
                /* A file path: easy enough */
                return File(dirURL.toURI()).list()?.toList()
            }
            if (dirURL == null) {
                /*
                 * In case of a jar file, we can't actually find a directory.
                 * Have to assume the same jar as clazz.
                 */
                val me = clazz.name.replace(".", "/") + ".class"
                dirURL = clazz.classLoader.getResource(me)
            }
            if (dirURL.protocol.equals("jar")) {
                /* A JAR path */
                val jarPath: String = dirURL.path.substring(5, dirURL.path.indexOf("!")) //strip out only the JAR file
                val jar = JarFile(URLDecoder.decode(jarPath, "UTF-8"))
                return jar.entries().toList().filter {
                    it.name.startsWith(path.trimStart('/')) && it.name.trim('/') != path.trim('/')
                }.map {
                    it.name.removePrefix(path.trimStart('/')).trimStart('/')
                }
            }
            throw UnsupportedOperationException("Cannot list files for URL $dirURL")
        }

        fun getResourceAsString(pathInResourceFolder: String): String? {
            return readResourceAsStream(pathInResourceFolder)?.bufferedReader()?.readText()
        }

        fun getMergedJsonFiles(path: String): String {

            val files = getResourceFiles(path).mapNotNull {
                getResourceAsString("$path/$it")?.trim(' ', '\r', '\n')?.trimStart('{')?.trimEnd('}')?.trimEnd(',')
            }
            return "{" + files.joinToString(",") + "}"
        }
    }
}