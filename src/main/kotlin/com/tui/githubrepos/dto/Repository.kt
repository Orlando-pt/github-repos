package com.tui.githubrepos.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Repository(
    val name: String,
    val owner: Owner,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val fork: Boolean,
    var branches: List<Branch> = emptyList(),
)

data class Owner(
    val login: String,
)

data class Branch(
    val name: String,
    val commit: Commit,
)

data class Commit(
    val sha: String,
)
