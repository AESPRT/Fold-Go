package com.aesprt.foldgo.domain.model

data class Shop(
    val shopId: String,
    val name: String,
    val address: String,
    val mobileNumber: String = "",
    val ownerId: String,
    val pin: String,
    val settings: Map<String, String>,
    val createdAt: Long
)
