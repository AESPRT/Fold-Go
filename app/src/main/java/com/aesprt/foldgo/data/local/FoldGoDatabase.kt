package com.aesprt.foldgo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aesprt.foldgo.data.local.dao.*
import com.aesprt.foldgo.data.local.entities.models.*

@Database(
    entities = [
        ShopEntity::class,
        MachineEntity::class,
        OrderEntity::class,
        InventoryEntity::class,
        SyncOutboxEntity::class,
        StaffEntity::class,
        MachineCategoryEntity::class,
        ServiceEntity::class,
        OrderBatchEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoldGoDatabase : RoomDatabase() {
    abstract val shopDao: ShopDao
    abstract val orderDao: OrderDao
    abstract val machineDao: MachineDao
    abstract val inventoryDao: InventoryDao
    abstract val staffDao: StaffDao
    abstract val machineCategoryDao: MachineCategoryDao
    abstract val serviceDao: ServiceDao
    abstract val orderBatchDao: OrderBatchDao
}
