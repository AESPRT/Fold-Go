package com.aesprt.foldgo.data.local

import androidx.room.TypeConverter
import com.aesprt.foldgo.domain.model.enums.MachineStatus
import com.aesprt.foldgo.domain.model.enums.MachineType
import com.aesprt.foldgo.domain.model.enums.OrderStatus

class Converters {
    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter
    fun fromMachineType(value: MachineType): String = value.name

    @TypeConverter
    fun toMachineType(value: String): MachineType = MachineType.valueOf(value)

    @TypeConverter
    fun fromMachineStatus(value: MachineStatus): String = value.name

    @TypeConverter
    fun toMachineStatus(value: String): MachineStatus = MachineStatus.valueOf(value)
}
