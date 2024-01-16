package com.tui.githubrepos.controller

import com.tui.githubrepos.dto.Repository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RepositoryControllerTestIT {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `Should get list of repositories when user has any`() {
        val username = "Orlando-pt"
        val result = testRestTemplate.getForObject(
            "/api/repository/$username",
            Array<Repository>::class.java
        )

        Assertions.assertNotNull(result)

        for (repository in result!!) {
            Assertions.assertEquals(username, repository.owner.login)
        }
    }
}