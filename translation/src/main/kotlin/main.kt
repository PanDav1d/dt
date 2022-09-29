package com.logenios.plugins

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.util.*


fun main() {
    GenerateLanguageFilePlugin().generateLanguageFile(File(System.getProperty("user.dir")))
}





class MissingTranslationFromFiles(message: String) : Exception(message)
class MissingValuesInJsonObject(message: String) : Exception(message)
class DuplicatePathsInFiles(message: String) : Exception(message)
class AmbiguousPathName(message: String) : Exception(message)
class MissingPath(message: String) : Exception(message)
val findGoosefootRegex = Regex("[\"|\"\"\"]([\\s\\S]*?)[\"|\"\"\"]")


data class Translation(
    val translations: MutableList<TranslationForFile> = mutableListOf()
) {
    fun checkForErrors() {
        val missingTranslationsInFiles = translations.mapNotNull {
            if (it.extractedTranslationCalls.size != it.numberOfFoundI18nCalls
            ) {
                "Found only ${it.extractedTranslationCalls.size} instead ${it.numberOfFoundI18nCalls} in file ${it.filePath}\n" +
                        "Extracted translation calls: ${it.extractedTranslationCalls}"
            } else null
        }
        if (missingTranslationsInFiles.isNotEmpty()) {
            throw MissingTranslationFromFiles(missingTranslationsInFiles.joinToString("\n"))
        }

        val translationCalls = getTranslationCalls()
        val paths = getPaths()

        val missingPathErrors = translationCalls.filter { it.path == null }.map { translationCall ->
            "Missing path in translation call ${translationCall.filePath}:${translationCall.line} in range ${translationCall.range}"
        }
        if (missingPathErrors.isNotEmpty()) {
            throw MissingPath(missingPathErrors.joinToString("\n"))
        }

        val duplicatePathsErrors = paths.distinct().mapNotNull { path ->
            val filtered = translationCalls.filter { it.path == path }
            if (filtered.size > 1) {
                "Found duplicate path $path in translation calls: ${filtered.map { "\n${it.filePath}:${it.line} in range ${it.range}" }}"
            } else null
        }

        val pathConflictErrors = paths.distinct().filterNotNull().mapNotNull {

            val original = translationCalls.find { tc-> tc.path == it }

            val pathConflict = translationCalls.find { tc ->
                tc.path?.startsWith(it) == true &&
                        tc.path != it &&
                        tc.path.removePrefix(it)[0] == '.'
            }
            if(pathConflict != null){
                "Found common prefix ${it}. It conflicts with path ${pathConflict.path}. \n${pathConflict.filePath}:${pathConflict.line}\n${original?.filePath}:${original?.line}"
            } else {
                null
            }
        }


        if (duplicatePathsErrors.isNotEmpty()) {
            throw DuplicatePathsInFiles(duplicatePathsErrors.joinToString("\n"))
        }

        if(pathConflictErrors.isNotEmpty()){
            throw AmbiguousPathName(pathConflictErrors.joinToString("\n"))
        }

        val numberOfFoundI18nCalls = getNumberOfI18nCalls()
        val translationsInJsonObject = this.toJsonObject().countEntriesRecursive()
        if (translationsInJsonObject != numberOfFoundI18nCalls) {
            throw MissingValuesInJsonObject("Found $numberOfFoundI18nCalls calls of I18n::t() but only $translationsInJsonObject translations in json")
        }

    }

    private fun getPaths() = this.getTranslationCalls().map { it.path }

    private fun getTranslationCalls(): List<TranslationCall> {
        return this.translations.flatMap { it.extractedTranslationCalls }
    }

    private fun getNumberOfI18nCalls() = translations.map { it.numberOfFoundI18nCalls }.sum()

    fun toJsonObject(): JsonObject {
        return translations.flatMap { it.extractedTranslationCalls }.toJsonObject()
    }
}

private fun JsonObject.countEntriesRecursive(): Int {
    return this.entrySet().map {
        if (it.value is JsonObject) {
            (it.value as JsonObject).countEntriesRecursive()
        } else {
            1
        }
    }.sum()
}
private fun tripleGoosefoot() = "\"\"\""

fun findI18nCalls(file: File): TranslationForFile {
    val fileContent = file.readText()
    return findI18nCallsInString(fileContent, file.absolutePath)
}

fun findI18nCallsInString(fileContent: String, filePath: String): TranslationForFile {
    val findI18nRegex = Regex("(I18n\\.t\\()")
    val foundI18nResult = findI18nRegex.findAll(fileContent).toMutableList()

    val translationCalls = foundI18nResult.map { matchResult ->
        val textInBracket = fileContent.findTextInFirstBracket(matchResult.range.last).replace(tripleGoosefoot(), "\"")

        val splitRegex = Regex("\"(\\s|)*,")

        val splitted = splitRegex.split(textInBracket).map {
            if (it.endsWith('"')) {
                it
            } else {
                it.plus("\"")
            }
        }
        val textRegex = Regex("text([\\s\\S]*)=([\\s\\S]*)\"")
        val defaultTextRaw = splitted.find { it.contains(textRegex) }
        val defaultText = defaultTextRaw?.let { findGoosefootRegex.find(it)?.groupValues?.get(1) }

        val pathRegex = Regex("path([\\s\\S]*)=([\\s\\S]*)\"")
        val pathTextRaw = splitted.find { it.contains(pathRegex) }
        val pathText = pathTextRaw?.let { findGoosefootRegex.find(it)?.groupValues?.get(1) }

        if(defaultText != null && pathText != null){
            val t = TranslationCall(
                filePath = filePath,
                range = IntRange(matchResult.range.first, matchResult.range.first + textInBracket.length + 1),
                line = fileContent.substring(0, matchResult.range.first).countLines() + 1,
                extractedParameterText = textInBracket,
                defaultText = defaultText,
                path = pathText
            )
            println("I18n.t at index ${t.range} -> ${t.path} : ${t.defaultText}")
            t
        }
        else {
            val firstParamValue = splitted[0].let { findGoosefootRegex.find(it)?.groupValues?.get(1) }
            val secondParamValue = splitted[1].let { findGoosefootRegex.find(it)?.groupValues?.get(1) }

            val t = TranslationCall(
                filePath = filePath,
                range = IntRange(matchResult.range.first, matchResult.range.first + textInBracket.length + 1),
                line = fileContent.substring(0, matchResult.range.first).countLines() + 1,
                extractedParameterText = textInBracket,
                defaultText = secondParamValue,
                path = firstParamValue
            )
            println("I18n.t at index ${t.range} -> ${t.path} : ${t.defaultText}")
            t
        }
    }
    return TranslationForFile(
        filePath = filePath,
        numberOfFoundI18nCalls = foundI18nResult.size,
        extractedTranslationCalls = translationCalls
    )
}

data class TranslationForFile(
    val filePath: String,
    val numberOfFoundI18nCalls: Int,
    val extractedTranslationCalls: List<TranslationCall>
)

data class TranslationCall(
    val filePath: String,
    val range: IntRange,
    val line: Long,
    val extractedParameterText: String,
    val path: String?,
    val defaultText: String?
)

fun List<TranslationCall>.toJsonObject(): JsonObject {
    val jsonObject = JsonObject()
    this.forEach { translationCall ->

        var selectedJsonElement: JsonObject = jsonObject
        val splittedPath = translationCall.path?.split(".")
        splittedPath?.forEachIndexed { idx, pathEntry ->
            if (idx == splittedPath.size-1) {
                selectedJsonElement.addProperty(pathEntry, translationCall.defaultText)
            } else {
                selectedJsonElement = if (selectedJsonElement.has(pathEntry)) {
                    selectedJsonElement.get(pathEntry) as JsonObject
                } else {
                    val newObject = JsonObject()
                    selectedJsonElement.add(pathEntry, newObject)
                    newObject
                }
            }
        }
    }
    return jsonObject
}

class GenerateLanguageFilePlugin {


    fun generateLanguageFile(rootDir: File) {
        val translation = Translation()

        rootDir.walkTopDown().filter { file ->
            !file.path.contains("src" + File.separator + "test" + File.separator) &&
                    !file.path.contains(File.separator + "build" + File.separator) &&
                    !file.path.contains("xconvert-generated") &&
                    !file.path.contains("auto-generated") &&
                    !file.path.contains("generatedsources") &&
                    !file.name.contains("GenerateLanguageFilePlugin.kt") &&
                    (file.extension == "java" || file.extension == "kt")
        }.forEach { file ->
            println("Working on file " + file.absolutePath)
            translation.translations.add(
                findI18nCalls(file)
            )
        }

        translation.copy(translations = translation.translations.filter { it.numberOfFoundI18nCalls != 0 }.toMutableList()).writeToFileAndPrint("${rootDir}/xconvert-base/src/main/resources/locales/deu_debug.json")

        val jsonObject = translation.toJsonObject()
        jsonObject.writeToFileAndPrint("${rootDir}/translation/src/main/resources/locales/de/main.json")
        translation.checkForErrors()
    }
}

private fun String.countLines(): Long {
    val countLinesRegex = Regex("(\\r\\n)")
    return countLinesRegex.findAll(this).count().toLong()
}

fun Any.writeToFileAndPrint(path: String) {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    val outputFile = File(path)
    outputFile.parentFile.mkdirs()
    outputFile.writeText(gson.toJson(this))
    println("New translation file: ${outputFile.absolutePath}:\n" + outputFile.readText())
}

fun Char.isBracket(): Boolean {
    return listOf('(', ')', '{', '}', '[', ']').contains(this)
}

fun Char.matchesBracket(peek: Char): Boolean {
    val pair = listOf(this, peek)
    val possiblePairs = listOf(
        listOf('[', ']'),
        listOf('(', ')'),
        listOf('{', '}')
    )
    return possiblePairs.contains(pair) || possiblePairs.contains(pair.reversed())
}

fun String.findTextInFirstBracket(startIndex: Int = 0): String {
    val string = this.substring(startIndex)

    val stack = Stack<Char>()
    var firstBracketIndex: Int? = null
    var lastBracketIndex: Int? = null

    string.forEachIndexed { index, c ->
        if (c.isBracket()) {
            if (stack.isNotEmpty() && c.matchesBracket(stack.peek())) {
                stack.pop()
            } else {
                stack.push(c)
                if (firstBracketIndex == null) {
                    firstBracketIndex = index
                }
            }
        }

        if (stack.isEmpty() && firstBracketIndex != null) {
            lastBracketIndex = index
            return string.substring(firstBracketIndex!! + 1, lastBracketIndex!!)
        }
    }
    return ""
}