package com.aesprt.foldgo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aesprt.foldgo.data.local.dao.InventoryDao
import com.aesprt.foldgo.data.local.dao.MachineDao
import com.aesprt.foldgo.data.local.dao.OrderDao
import com.aesprt.foldgo.data.local.dao.ShopDao
import com.aesprt.foldgo.data.local.entities.*

@Database(
    entities = [
        ShopEntity::class,
        MachineEntity::class,
        OrderEntity::class,
        InventoryEntity::class,
        SyncOutboxEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoldGoDatabase : RoomDatabase() {
    abstract val shopDao: ShopDao
    abstract val orderDao: OrderDao
    abstract val machineDao: MachineDao
    abstract val inventoryDao: InventoryDao
}
