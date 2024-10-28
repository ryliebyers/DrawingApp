package com.example


import kotlinx.serialization.Serializable

@Serializable
data class ExposedUser(
    val id: Int? = null,
    val name: String,
    val age: Int
)
