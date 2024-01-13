package com.tui.githubrepos.exception

/**
 * Custom response for Exceptions
 * @param status HTTP status code
 * @param message Error message
 */
data class ErrorResponse(
    val status: Int,
    val message: String
)
