package com.tui.githubrepos.exception

/**
 * Generic exception for HTTP client errors
 * @param message Error message
 * @param status HTTP status code
 */
class HttpClientException(
    override val message: String,
    val status: Int
) : RuntimeException(
    message
)