package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "machine_categories")
data class MachineCategoryEntity(
    @PrimaryKey val categoryId: String,
    val name: String,
    val iconName: String? = null,
    val colorHex: String? = null
)
