package com.example

import io.ktor.server.auth.Principal

data class MyAuthenticatedUser(
    val id: String
) : Principal