package com.tui.githubrepos.service

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.httpclient.GithubClient
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service

/**
 * Service to get information on GitHub repositories
 */
@Service
class RepositoryService(
    private val githubClient: GithubClient
) {
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
                    .collectList()
                    .map {
                        repository.branches = it
                        repository
                    }
            }
            .collectList()
            .awaitSingle()
            .orEmpty()

        return createdRepositories

    }
}