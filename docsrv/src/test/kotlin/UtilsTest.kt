import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import de.doctag.docsrv.sanitizeUrl

class SanitizeUrlTest {

    @Test
    fun testSanitizeUrl() {
        // Einige Testfälle
        val testCases = mapOf(
            "http://example.com" to "example.com",
            "https://example.com" to "example.com",
            "http://example.com/" to "example.com",
            "https://example.com/" to "example.com",
            "http://sub.example.com/path" to "sub.example.com/path",
            "example.com/" to "example.com",
            "example.com" to "example.com",
            "https://sub.example.com/path/" to "sub.example.com/path",
            "http://sub.example.com/path/" to "sub.example.com/path"
        )

        // Durchlaufen aller Testfälle und Überprüfung, ob die Methode das erwartete Ergebnis liefert
        for ((input, expected) in testCases) {
            val result = sanitizeUrl(input)
            assertEquals(expected, result, "Failed for input: $input")
        }
    }
}
