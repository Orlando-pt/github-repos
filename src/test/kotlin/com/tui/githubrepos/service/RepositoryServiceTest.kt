package com.tui.githubrepos.service

import com.tui.githubrepos.dto.Branch
import com.tui.githubrepos.dto.Commit
import com.tui.githubrepos.dto.Owner
import com.tui.githubrepos.dto.Repository
import com.tui.githubrepos.httpclient.GithubClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux

@ExtendWith(SpringExtension::class)
class RepositoryServiceTest {

    @Mock
    private lateinit var githubClient: GithubClient

    @InjectMocks
    private lateinit var repositoryService: RepositoryService

    private val repo1 = Repository(
        name = "repo1",
        owner = Owner(
            login = "username"
        ),
        fork = false
    )

    private val repo2 = Repository(
        name = "repo2",
        owner = Owner(
            login = "username"
        ),
        fork = false
    )

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `Should get empty list when user has no repositories`() {
        Mockito.`when`(githubClient.getAllRepositories("username")).thenReturn(Flux.empty())

        runBlocking {
            val result = repositoryService.getAllRepositories("username")
            Assertions.assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `Should get repositories with empty list of branches when it was just created`() {
        Mockito.`when`(githubClient.getAllRepositories("username")).thenReturn(
            Flux.just(repo1, repo2)
        )

        Mockito.`when`(githubClient.getAllRepositoryBranches(repo1.owner.login, repo1.name)).thenReturn(
            Flux.empty()
        )
        Mockito.`when`(githubClient.getAllRepositoryBranches(repo2.owner.login, repo2.name)).thenReturn(
            Flux.empty()
        )

        runBlocking {
            val result = repositoryService.getAllRepositories("username")
            Assertions.assertEquals(2, result.size)
            Assertions.assertTrue(result[0].branches.isEmpty())
            Assertions.assertTrue(result[1].branches.isEmpty())
        }
    }

    @Test
    fun `Should get repositories with branches when they already have them`() {
        val repoWithBranches = repo1.copy(
            branches = listOf(Branch("branch1", Commit("sha")), Branch("branch2", Commit("sha")))
        )
        Mockito.`when`(githubClient.getAllRepositories("username")).thenReturn(
            Flux.just(repo1, repo2)
        )

        Mockito.`when`(githubClient.getAllRepositoryBranches(repo1.owner.login, repo1.name)).thenReturn(
            Flux.empty()
        )
        Mockito.`when`(githubClient.getAllRepositoryBranches(repo2.owner.login, repo2.name)).thenReturn(
            Flux.just(Branch("branch1", Commit("sha")), Branch("branch2", Commit("sha")))
        )

        runBlocking {
            val result = repositoryService.getAllRepositories("username")
            Assertions.assertEquals(2, result.size)
            Assertions.assertEquals(0, result[0].branches.size)
            Assertions.assertEquals(2, result[1].branches.size)

            Assertions.assertIterableEquals(repoWithBranches.branches, result[1].branches)
        }
    }
}