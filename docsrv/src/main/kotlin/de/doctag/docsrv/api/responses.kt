package de.doctag.docsrv.api

import de.doctag.docsrv.model.Document
import de.doctag.docsrv.model.FileData

data class HealthCheckResponse(
        val isHealthy: Boolean
)

data class DiscoveryResponse(
        val identity: String
)

data class EmbeddedDocument(
        val files: List<FileData>,
        val document: Document
)