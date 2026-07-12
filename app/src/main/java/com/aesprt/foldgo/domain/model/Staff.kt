package com.aesprt.foldgo.domain.model

data class Staff(
    val staffId: String,
    val shopId: String,
    val name: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: Long
)
