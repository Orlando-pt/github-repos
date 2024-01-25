package com.tui.githubrepos.controller

import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.exception.ErrorResponse
import com.tui.githubrepos.util.Utils
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ActiveProfiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RepositoryControllerTestIT {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    private lateinit var mockGitHubServer: MockWebServer

    @BeforeAll
    fun setup() {
        mockGitHubServer = MockWebServer()
        mockGitHubServer.start(54234)
    }

    @AfterAll
    fun tearDown() {
        mockGitHubServer.shutdown()
    }

    @Test
    fun `Should get list of repositories when user has any`() {
        val username = "Orlando-pt"
        val jsonRepositories = Utils.getJsonFileContent("repositories.json")
        val jsonBranches = Utils.getJsonFileContent("repository-ads-branches.json")

        mockGitHubServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/users/$username/repos" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(jsonRepositories)
                    }

                    "/repos/$username/ads/branches" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(jsonBranches)
                    }

                    "/repos/$username/aoc/branches" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("[]")
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        val result = testRestTemplate.getForObject(
            "/api/repository/$username",
            Array<Repository>::class.java
        )!!

        Assertions.assertThat(result.size).isEqualTo(2)

        Assertions.assertThat(result[0].name).isEqualTo("ads")
        Assertions.assertThat(result[0].owner.login).isEqualTo(username)
        Assertions.assertThat(result[0].branches)
            .hasSize(4)
            .extracting("name")
            .containsExactlyInAnyOrder(
                "master",
                "feature/#14-export_tree_as_graphviz",
                "feature/queries-update",
                "feature/export_and_load_places"
            )

        Assertions.assertThat(result[1].name).isEqualTo("aoc")
        Assertions.assertThat(result[1].owner.login).isEqualTo(username)
        Assertions.assertThat(result[1].branches.size).isEqualTo(0)

        val request1 = mockGitHubServer.takeRequest()
        Assertions.assertThat(request1.path).isEqualTo("/users/$username/repos")
        Assertions.assertThat(request1.method).isEqualTo("GET")

        val request2 = mockGitHubServer.takeRequest()
        Assertions.assertThat(request2.path).isEqualTo("/repos/$username/ads/branches")
        Assertions.assertThat(request2.method).isEqualTo("GET")

        val request3 = mockGitHubServer.takeRequest()
        Assertions.assertThat(request3.path).isEqualTo("/repos/$username/aoc/branches")
        Assertions.assertThat(request3.method).isEqualTo("GET")
    }

    @Test
    fun `Should get list of repositories with empty branches when an error occurs fetching them`() {
        val username = "Orlando-pt"
        val jsonRepositories = Utils.getJsonFileContent("repositories.json")

        mockGitHubServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/users/$username/repos" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody(jsonRepositories)
                    }

                    "/repos/$username/ads/branches" -> {
                        MockResponse()
                            .setResponseCode(500)
                            .setHeader("Content-Type", "application/json")
                            .setBody("")
                    }

                    "/repos/$username/aoc/branches" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setHeader("Content-Type", "application/json")
                            .setBody("[]")
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        val result = testRestTemplate.getForObject(
            "/api/repository/$username",
            Array<Repository>::class.java
        )!!

        Assertions.assertThat(result.size).isEqualTo(2)

        Assertions.assertThat(result[0].name).isEqualTo("ads")
        Assertions.assertThat(result[0].owner.login).isEqualTo(username)
        Assertions.assertThat(result[0].branches).hasSize(0)

        Assertions.assertThat(result[1].name).isEqualTo("aoc")
        Assertions.assertThat(result[1].owner.login).isEqualTo(username)
        Assertions.assertThat(result[1].branches.size).isEqualTo(0)

        val request1 = mockGitHubServer.takeRequest()
        Assertions.assertThat(request1.path).isEqualTo("/users/$username/repos")
        Assertions.assertThat(request1.method).isEqualTo("GET")

        val request2 = mockGitHubServer.takeRequest()
        Assertions.assertThat(request2.path).isEqualTo("/repos/$username/ads/branches")
        Assertions.assertThat(request2.method).isEqualTo("GET")

        val request3 = mockGitHubServer.takeRequest()
        Assertions.assertThat(request3.path).isEqualTo("/repos/$username/aoc/branches")
        Assertions.assertThat(request3.method).isEqualTo("GET")
    }

    @Test
    fun `Should get Not Found response when user does not exist`() {
        val username = "JohnDoe"

        mockGitHubServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/users/$username/repos" -> {
                        MockResponse()
                            .setResponseCode(404)
                            .setHeader("Content-Type", "application/json")
                            .setBody("")
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        val result = testRestTemplate.getForObject(
            "/api/repository/$username",
            ErrorResponse::class.java
        )!!

        Assertions.assertThat(result.message).isEqualTo("Username not found: $username")
        Assertions.assertThat(result.status).isEqualTo(404)

        val request = mockGitHubServer.takeRequest()
        Assertions.assertThat(request.path).isEqualTo("/users/$username/repos")
        Assertions.assertThat(request.method).isEqualTo("GET")
    }

    @Test
    fun `Should get Http Client Exception response when GitHub client gives an error`() {
        val username = "JohnDoe"

        mockGitHubServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    "/users/$username/repos" -> {
                        MockResponse()
                            .setResponseCode(500)
                            .setHeader("Content-Type", "application/json")
                            .setBody("")
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        val result = testRestTemplate.getForObject(
            "/api/repository/$username",
            ErrorResponse::class.java
        )!!

        Assertions.assertThat(result.message).isEqualTo("Error fetching repositories for username: $username")
        Assertions.assertThat(result.status).isEqualTo(500)

        val request = mockGitHubServer.takeRequest()
        Assertions.assertThat(request.path).isEqualTo("/users/$username/repos")
        Assertions.assertThat(request.method).isEqualTo("GET")
    }
}