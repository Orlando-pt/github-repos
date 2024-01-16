package com.tui.githubrepos.controller

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.service.RepositoryService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to get information on GitHub repositories
 */
@RestController
@RequestMapping("/repository")
class RepositoryController(
    private val repositoryService: RepositoryService
) {

    /**
     * Get all user repositories
     * @param username GitHub username
     * @return List of repositories
     */
    @GetMapping("/{username}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getAllRepositories(
        @PathVariable username: String
    ): List<Repository> {
        return repositoryService.getAllRepositories(username)
    }
}