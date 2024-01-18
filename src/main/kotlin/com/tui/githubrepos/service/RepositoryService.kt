package com.tui.githubrepos.service

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.httpclient.GithubClient
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 * Service to get information on GitHub repositories
 */
@Service
class RepositoryService(
    private val githubClient: GithubClient
) {
    private val log: Logger = LoggerFactory.getLogger(RepositoryService::class.java)

    /**
     * Get all user repositories
     * @param username GitHub username
     * @return List of repositories
     */
    suspend fun getAllRepositories(username: String): List<Repository> {
        val createdRepositories = githubClient
            .getAllRepositories(username)
            .filter {
                !it.fork
            }
            .flatMap { repository ->
                githubClient
                    .getAllRepositoryBranches(
                        repository.owner.login,
                        repository.name
                    )
                    .onErrorResume {
                        // Fail silently if we can't get branches for a repository
                        Flux.empty()
                    }
                    .collectList()
                    .map {
                        repository.branches = it
                        repository
                    }
            }
            .collectList()
            .awaitSingle()
            .orEmpty()

        log.info("Found ${createdRepositories.size} repositories for username: $username")

        return createdRepositories

    }
}