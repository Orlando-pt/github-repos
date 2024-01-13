package com.tui.githubrepos.controller

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.service.RepositoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/repository")
class RepositoryController(
    private val repositoryService: RepositoryService
) {

    //    @GetMapping(consumes = ["application/json"], produces = ["application/json"])
    @GetMapping(produces = ["application/json"])
    fun getAllRepositories(): List<Repository> {
        return repositoryService.getAllRepositories()
    }
}