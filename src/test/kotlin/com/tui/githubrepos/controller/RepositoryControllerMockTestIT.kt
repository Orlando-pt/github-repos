package com.tui.githubrepos.controller

import com.tui.githubrepos.dto.Branch
import com.tui.githubrepos.dto.Commit
import com.tui.githubrepos.dto.Owner
import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.exception.HttpClientException
import com.tui.githubrepos.exception.ResourceNotFoundException
import com.tui.githubrepos.service.RepositoryService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.aot.DisabledInAotMode
import org.springframework.test.web.reactive.server.WebTestClient

// Follow this GitHub issue to see when the @DisabledInAotMode stops to be needed:
// https://github.com/spring-projects/spring-boot/issues/36997
@DisabledInAotMode
@WebFluxTest(controllers = [RepositoryController::class])
@AutoConfigureWebTestClient
class RepositoryControllerMockTestIT {

    @MockBean
    private lateinit var repositoryService: RepositoryService

    @Autowired
    private lateinit var webClient: WebTestClient

    private val repository = Repository(
        name = "repo1",
        owner = Owner(
            login = "username"
        ),
        branches = listOf(
            Branch(
                name = "master",
                commit = Commit(
                    sha = "sha",
                )
            )

        ),
        fork = false
    )

    @Test
    fun `Should get repositories when the username exists on GitHub`() {
        val repositoryNoBranches = repository.copy(branches = listOf())

        runBlocking {
            Mockito.`when`(repositoryService.getRepositories(repository.owner.login)).thenReturn(
                listOf(repository, repositoryNoBranches)
            )

            webClient.get()
                .uri("/repository/${repository.owner.login}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("size()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo(repository.name)
                .jsonPath("$[0].owner.login").isEqualTo(repository.owner.login)
                .jsonPath("$[0].branches.size()").isEqualTo(1)
                .jsonPath("$[0].branches[0].name").isEqualTo(repository.branches[0].name)
                .jsonPath("$[0].branches[0].commit.sha").isEqualTo(repository.branches[0].commit.sha)
                .jsonPath("$[0].fork").doesNotExist()
                .jsonPath("$[1].name").isEqualTo(repositoryNoBranches.name)
                .jsonPath("$[1].owner.login").isEqualTo(repositoryNoBranches.owner.login)
                .jsonPath("$[1].branches.size()").isEqualTo(0)
                .jsonPath("$[1].branches[0].name").doesNotExist()
                .jsonPath("$[1].fork").doesNotExist()

            Mockito.verify(repositoryService, Mockito.times(1)).getRepositories(repository.owner.login)
        }
    }

    @Test
    fun `Should get 404 response when the username does not exist on GitHub`() {
        val username = "Orlando-pt"

        runBlocking {
            Mockito.`when`(repositoryService.getRepositories(username)).thenThrow(
                ResourceNotFoundException("Username not found: $username")
            )
            webClient.get()
                .uri("/repository/$username")
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("status").isEqualTo(404)
                .jsonPath("message").isEqualTo("Username not found: $username")

            Mockito.verify(repositoryService, Mockito.times(1)).getRepositories(username)
        }
    }

    @Test
    fun `Should get an error response when an unexpected error occurs fetching repositories from the GitHub API`() {
        val username = "Orlando-pt"
        val errorMessage = "Error fetching repositories for username: $username"

        runBlocking {
            Mockito.`when`(repositoryService.getRepositories(username)).thenThrow(
                HttpClientException(errorMessage, 401)
            )
            webClient.get()
                .uri("/repository/$username")
                .exchange()
                .expectStatus().isUnauthorized
                .expectBody()
                .jsonPath("status").isEqualTo(401)
                .jsonPath("message").isEqualTo(errorMessage)

            Mockito.verify(repositoryService, Mockito.times(1)).getRepositories(username)
        }
    }

    @Test
    fun `Should get 406 response when the media type is not supported`() {
        val username = "Orlando-pt"
        runBlocking {
            webClient.get()
                .uri("/repository/$username")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(406)
                .expectBody()
                .jsonPath("status").isEqualTo(406)
                .jsonPath("error").isEqualTo("Not Acceptable")
                .jsonPath("path").isEqualTo("/repository/$username")

            Mockito.verify(repositoryService, Mockito.times(0)).getRepositories(username)
        }
    }
}