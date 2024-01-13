package com.tui.githubrepos.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception thrown when a resource is not found
 * @param message Error message
 */
@ResponseStatus(reason = "Not found", code = HttpStatus.NOT_FOUND)
class ResourceNotFoundException(
    override val message: String
) : RuntimeException(message)