package com.aesprt.foldgo.domain.model

import com.aesprt.foldgo.domain.model.enums.ServiceScope

data class AddOn(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val appliesTo: ServiceScope,
    val isActive: Boolean
)
