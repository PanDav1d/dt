package de.doctag.docsrv

import java.lang.Exception

class BadRequest(message: String) : Exception(message)

class NotFound(message: String) : Exception(message)