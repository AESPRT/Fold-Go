package com.aesprt.foldgo.core.util

import java.util.UUID
import kotlin.random.Random

object IdGeneratorUtils {

    fun generateShopId(): String {
        return "sp-${generateRandomNumber()}"
    }

    fun generateCustomerId(): String {
        return "cs-${generateRandomNumber()}"
    }

    fun generateStaffId(): String {
        return "s-${generateRandomNumber()}"
    }

    fun generateOrderId(): String {
        return "ord-${generateRandomNumber()}"
    }

    fun generateMachineId(): String {
        return "m-${generateRandomNumber()}"
    }

    private fun generateRandomNumber(length: Int = 6): String {
        val chars = "0123456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun generateUniqueId(prefix: String): String {
        return "$prefix-${UUID.randomUUID().toString().take(8)}"
    }
}
