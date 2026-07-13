package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val shopId: String,
    val name: String,
    val address: String,
    val mobileNumber: String = "",
    val ownerId: String,
    val pin: String,
    val settings: String,
    val createdAt: Long
)
