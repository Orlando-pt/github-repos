package com.tui.githubrepos.util

class Utils {
    companion object {
        fun getJsonFileContent(fileName: String): String {
            return this::class.java.getResource("/json/$fileName")?.readText() ?: ""
        }
    }
}