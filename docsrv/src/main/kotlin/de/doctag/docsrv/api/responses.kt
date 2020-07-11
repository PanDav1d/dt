package de.doctag.docsrv.api

data class HealthCheckResponse(
        val isHealthy: Boolean
)

data class DiscoveryResponse(
        val identity: String
)