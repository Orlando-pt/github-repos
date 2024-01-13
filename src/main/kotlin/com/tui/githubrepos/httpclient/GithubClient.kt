package com.tui.githubrepos.httpclient

import com.tui.githubrepos.dto.Branch
import com.tui.githubrepos.dto.Repository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GithubClient(
    @Value("\${github.url}")
    private val githubUrl: String,

    @Value("\${github.token}")
    private val githubToken: String
) {

    private val client = WebClient.builder()
        .baseUrl(githubUrl)
        .defaultHeaders { headers -> headers.addAll(getHeaders()) }
        .build()

    fun getAllRepositories(username: String) = client
        .get()
        .uri("/users/$username/repos")
        .retrieve()
        .bodyToFlux(Repository::class.java)

    fun getAllRepositoryBranches(owner: String, repository: String) = client
        .get()
        .uri("/repos/$owner/$repository/branches")
        .retrieve()
        .bodyToFlux(Branch::class.java)

    private fun getHeaders() = HttpHeaders().apply {
        set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        set(HttpHeaders.AUTHORIZATION, "bearer $githubToken")
        set("X-GitHub-Api-Version", "2022-11-28")
    }
}