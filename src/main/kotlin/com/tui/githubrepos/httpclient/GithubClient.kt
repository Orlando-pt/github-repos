package com.tui.githubrepos.httpclient

import com.tui.githubrepos.dto.Branch
import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.exception.HttpClientException
import com.tui.githubrepos.exception.ResourceNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component
class GithubClient(
    private val client: WebClient
) {
    private val log: Logger = LoggerFactory.getLogger(GithubClient::class.java)

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

            // Fail silently when fetching branches for a repository fails
            null
        }
        .awaitBodyOrNull<List<Branch>>() ?: emptyList()
}