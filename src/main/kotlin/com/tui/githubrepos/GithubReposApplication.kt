package com.tui.githubrepos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class GithubReposApplication

fun main(args: Array<String>) {
    runApplication<GithubReposApplication>(*args)
}
