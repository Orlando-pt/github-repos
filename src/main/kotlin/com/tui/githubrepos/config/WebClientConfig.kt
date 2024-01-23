package com.tui.githubrepos.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    @Value("\${github.url}")
    private val githubUrl: String,

    @Value("\${github.token}")
    private val githubToken: String
) {

    /**
     * Custom WebClient to make requests to GitHub API
     * @return WebClient
     */
    @Bean
    fun webClient() = WebClient.builder()
        .baseUrl(githubUrl)
        .defaultHeaders { headers -> headers.addAll(getHeaders()) }
        .build()

    /**
     * Get necessary headers for GitHub API requests
     * @return HttpHeaders
     */
    private fun getHeaders() = HttpHeaders().apply {
        set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        set(HttpHeaders.AUTHORIZATION, "bearer $githubToken")
        set("X-GitHub-Api-Version", "2022-11-28")
    }
}