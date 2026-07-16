package com.aesprt.foldgo.domain.model

data class Customer(
    val customerId: String,
    val name: String,
    val phone: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
