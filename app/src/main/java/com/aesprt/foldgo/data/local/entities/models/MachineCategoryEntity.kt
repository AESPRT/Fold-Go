package com.aesprt.foldgo.data.local.entities.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aesprt.foldgo.domain.model.enums.MachineType

@Entity(tableName = "machine_categories")
data class MachineCategoryEntity(
    @PrimaryKey val categoryId: String,
    val name: String,
    val type: MachineType,
    val iconName: String? = null,
    val colorHex: String? = null
)
