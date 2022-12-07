package de.doctag.docsrv.api

import de.doctag.docsrv.model.db
import de.doctag.docsrv.remotes.AttachmentImporter
import de.doctag.lib.logger
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktor.swagger.*
import ktor.swagger.version.shared.Group
import org.slf4j.LoggerFactory


val logger = LoggerFactory.getLogger("de.doctag.api.internal");
class CronResult(val success: Boolean)

fun Routing.internalRoutes() {
    @Group("App")
    @Location("/internal/cron/")
    class CronRequestCall()
    get<CronRequestCall>(
        "Check authentication".responds(
            ok<CronResult>()
        ).operationId("uploadWorkflowResultAndTriggerSignature")
    ) { req ->
        logger.info("Cron called")

        withContext(Dispatchers.IO){
            AttachmentImporter(db()).runImport()
        }

        call.respond(CronResult(success = true))
    }
}