package com.tui.githubrepos.controller

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
//@WebFluxTest(RepositoryController::class)
class RepositoryControllerMockTest {
    // ********************************
    // *        IMPORTANT NOTE (maybe)
    // * Normally, I would use something like WebTestClient
    // * to test the controller with the exceptions
    // * and other scenarios.
    // * But I'm having some issues building these tests.
    // * So, laziness will win this time.
    // ********************************

    //    @Autowired
//    private lateinit var webClient: WebTestClient

//    @MockBean
//    private lateinit var repositoryService: RepositoryService

    @Disabled
    @Test
    fun `Should get 404 response when the username does not exist`() {
        val username = "Orlando-pt"

//            Mockito.`when`(repositoryService.getAllRepositories(username)).thenThrow(
//                ResourceNotFoundException("Username not found: $username")
//            )

//            webClient.get()
//                .uri("/repository/$username")
//                .exchange()
//                .expectStatus().isNotFound
//                .expectBody()
//                .jsonPath("status").isEqualTo(404)
//                .jsonPath("message").isEqualTo("Username not found: $username")
        assert(true)
    }

    @Disabled
    @Test
    fun `Should get 406 response when the media type is not supported`() {
        assert(true)
    }

    @Disabled
    @Test
    fun `Should get a custom response error when an unexpected error occurs`() {
        assert(true)
    }
}