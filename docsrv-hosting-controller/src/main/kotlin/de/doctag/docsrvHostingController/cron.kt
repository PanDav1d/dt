package de.doctag.docsrvHostingController

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


private val logger = LoggerFactory.getLogger("de.docsrvHostingController.cron");

val cronThread by lazy {
    thread(isDaemon = true, start = false) {
        while (true) {
            try {
                logger.info("Cron Thread WakeUp")
                val instances = DbContext.hostedInstances.find().toList()

                logger.info("Calling ${instances.size} instances")

                instances.forEach { instance ->
                    try {
                        logger.info("Calling ${instance.domainName}/internal/cron")

                        val start = System.currentTimeMillis()
                        val url = URL("https://${instance.domainName}/internal/cron")
                        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                            val text = reader.readText()
                            val duration = System.currentTimeMillis() - start
                            logger.info("Took $duration ms. Result: $text")
                        }

                    } catch (ex: Exception) {
                        logger.error("Failed to cron call ${instance.domainName}")
                        logger.error(ex.message)
                    }
                }

            } catch (ex: Exception) {
                logger.error("Failed to cron. Retry later. Reason: ")
                logger.error(ex.message)
            } finally {
                Thread.sleep(1000 * 60 * 60)
            }
        }
    }
}