package com.tui.githubrepos.httpclient

import com.tui.githubrepos.dto.Branch
import com.tui.githubrepos.dto.Repository
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "githubClient", url = "https://api.github.com")
interface GithubClient {

    @GetMapping("/users/{username}/repos")
    fun getAllRepositories(@PathVariable username: String): List<Repository>

    @GetMapping("/repos/{owner}/{repository}/branches")
    fun getAllRepositoryBranches(
        @PathVariable owner: String,
        @PathVariable repository: String
    ): List<Branch>
}