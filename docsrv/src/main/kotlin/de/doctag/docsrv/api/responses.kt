package de.doctag.docsrv.api

import de.doctag.docsrv.model.*
import de.doctag.lib.toSha1HexString
import java.lang.Math.abs
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HealthCheckResponse(
        val isHealthy: Boolean
)

data class DiscoveryResponse(
        val identity: String
)

data class EmbeddedDocument(
        val files: List<FileData>,
        val document: Document
) {
    fun validateSignatures():Boolean {
        val actualFile = files.find{it._id == document.attachmentId}
        if(actualFile?.base64Content?.toSha1HexString() != document.attachmentId){
            kweb.logger.error("File Hash of DocTag-Document does not match. ${document.attachmentId} != ${actualFile?.base64Content?.toSha1HexString()}")
            return false
        }
        if(document.signatures?.all { sig->sig.isValid() } == false){
            kweb.logger.error("Not all signatures are valid")
            return false
        }
        val workflowAttachedFileIds = document.signatures?.flatMap { sig->sig.inputs?: listOf() }?.mapNotNull { it.fileId } ?: listOf()
        val allAttachmentsAreValid = workflowAttachedFileIds.map {attachmentId-> files.find{f->f._id == attachmentId} }.all { it != null && it._id == it.base64Content?.toSha1HexString() }

        if(!allAttachmentsAreValid){
            kweb.logger.error("Not all file attachment hashes matched the signed version")
            return false
        }

        val seenSignatures : MutableList<Signature> = mutableListOf()
        document.signatures?.forEach { sig->

            val expectedSigHash = seenSignatures.calculateSignatureHash()

            if(expectedSigHash != sig.doc?.previousSignaturesHash){
                kweb.logger.error("Expected signature hash does not match. Expected: ${expectedSigHash} != ${sig.doc?.previousSignaturesHash}")
                return false
            }

            seenSignatures.add(sig)
        }

        return true
    }
}