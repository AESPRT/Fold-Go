package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey val staffId: String,
    val shopId: String,
    val name: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Long
)
