package com.tui.githubrepos.httpclient

import com.tui.githubrepos.dto.Branch
import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.exception.HttpClientException
import com.tui.githubrepos.exception.ResourceNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component
class GithubClient(
    @Value("\${github.url}")
    private val githubUrl: String,

    @Value("\${github.token}")
    private val githubToken: String
) {
    private val log: Logger = LoggerFactory.getLogger(GithubClient::class.java)

    /**
     * Custom WebClient to make requests to GitHub API
     * @return WebClient
     */
    private val client = WebClient.builder()
        .baseUrl(githubUrl)
        .defaultHeaders { headers -> headers.addAll(getHeaders()) }
        .build()

    /**
     * Fetch all repositories from GitHub for a given username
     * @param username GitHub username
     * @return List of repositories
     */
    suspend fun getAllRepositories(username: String) = client
        .get()
        .uri("/users/$username/repos")
        .retrieve()
        .onStatus({ status -> status == HttpStatus.NOT_FOUND }) {
            throw ResourceNotFoundException("Username not found: $username")
        }
        .onStatus({ status -> status.isError }) {
            it.bodyToMono(String::class.java).let { body ->
                log.error("Error fetching repositories for username: $username. Response body: $body")
            }

            throw HttpClientException(
                "Error fetching repositories for username: $username",
                it.statusCode().value()
            )
        }
        .awaitBodyOrNull<List<Repository>>() ?: emptyList()

    /**
     * Fetch all branches for a given repository
     * @param owner Repository owner
     * @param repository Repository name
     * @return List of branches
     */
    suspend fun getAllRepositoryBranches(owner: String, repository: String) = client
        .get()
        .uri("/repos/$owner/$repository/branches")
        .retrieve()
        .onStatus({ status -> status.isError }) {
            it.bodyToMono(String::class.java).let { body ->
                log.error("Error fetching branches for repository: '$owner/$repository'. Response body: $body")
            }

            throw HttpClientException(
                "Error fetching branches for repository: '$owner/$repository'",
                it.statusCode().value()
            )
        }
        .awaitBodyOrNull<List<Branch>>() ?: emptyList()

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