package de.docward.docsrv

import java.lang.Exception

class BadRequest(message: String) : Exception(message)

class NotFound(message: String) : Exception(message)