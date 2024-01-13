package com.tui.githubrepos.exception.handler

import com.tui.githubrepos.exception.ErrorResponse
import com.tui.githubrepos.exception.HttpClientException
import com.tui.githubrepos.exception.ResourceNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Exception handler for the application
 * Handles exceptions thrown by the application
 */
@ControllerAdvice
class ExceptionHandler {
    private val log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

    @ExceptionHandler
    fun handleResourceNotFound(
        exception: ResourceNotFoundException
    ): ResponseEntity<ErrorResponse> {
        log.error(exception.message)

        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                message = exception.message
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler
    fun handleHttpClientException(
        exception: HttpClientException
    ): ResponseEntity<ErrorResponse> {
        log.error(exception.message)

        return ResponseEntity(
            ErrorResponse(
                status = exception.status,
                message = exception.message
            ),
            HttpStatus.valueOf(exception.status)
        )
    }
}