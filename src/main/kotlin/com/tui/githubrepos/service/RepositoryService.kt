package com.tui.githubrepos.service

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.httpclient.GithubClient
import org.springframework.stereotype.Service

@Service
class RepositoryService(
    private val githubClient: GithubClient
) {
    fun getAllRepositories(): List<Repository> {
        val repositories = githubClient.getAllRepositories("Orlando-pt")
        repositories
            .filter { !it.fork }
            .forEach { repository ->
                repository.branches = githubClient.getAllRepositoryBranches(
                    repository.owner.login,
                    repository.name
                )
            }
        return repositories
    }
}