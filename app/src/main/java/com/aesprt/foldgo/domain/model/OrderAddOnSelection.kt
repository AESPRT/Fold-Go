package com.aesprt.foldgo.domain.model

data class OrderAddOnSelection(
    val orderId: String,
    val addOnId: String,
    val priceAtTimeOfOrder: Double
)
