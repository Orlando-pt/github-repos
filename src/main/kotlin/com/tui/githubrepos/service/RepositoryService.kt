package com.tui.githubrepos.service

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.httpclient.GithubClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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
    suspend fun getRepositories(username: String): List<Repository> = coroutineScope {

        val repositories = async { githubClient.getAllRepositories(username) }.await()

        val originalRepositories = repositories.filter { !it.fork }

        log.info("Found ${originalRepositories.size} repositories for username: $username")

        originalRepositories.asFlow().map { repository ->
            async {
                githubClient.getAllRepositoryBranches(
                    repository.owner.login,
                    repository.name
                ).let {
                    repository.branches = it
                }

                repository
            }
        }.toList().awaitAll()
    }

}