package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.MachineType

data class MachineCategory(
    val categoryId: String,
    val name: String,
    val type: MachineType,
    val iconName: String? = null,
    val colorHex: String? = null
)
