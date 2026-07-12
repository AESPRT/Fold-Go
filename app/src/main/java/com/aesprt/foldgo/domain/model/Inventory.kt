package com.aesprt.foldgo.domain.model

data class Inventory(
    val itemId: String,
    val shopId: String,
    val name: String,
    val currentStock: Double,
    val unit: String,
    val lowStockThreshold: Double
)
