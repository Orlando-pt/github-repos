package com.tui.githubrepos.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignConfig {

    @Value("\${github.token}")
    private lateinit var token: String

    @Bean
    fun requestInterceptor() = RequestInterceptor { requestTemplate ->
        requestTemplate.header("Accept", "application/vnd.github+json")
        requestTemplate.header("X-GitHub-Api-Version", "2022-11-28")
        requestTemplate.header("Authorization", "Bearer $token")
    }
}