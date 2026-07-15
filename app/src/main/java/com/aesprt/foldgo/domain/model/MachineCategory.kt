package com.aesprt.foldgo.domain.model

data class MachineCategory(
    val categoryId: String,
    val name: String,
    val iconName: String? = null,
    val colorHex: String? = null
)
