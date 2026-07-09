package com.aesprt.foldgo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aesprt.foldgo.data.local.dao.OrderDao
import com.aesprt.foldgo.data.local.entities.*

@Database(
    entities = [
        ShopEntity::class,
        MachineEntity::class,
        OrderEntity::class,
        InventoryEntity::class,
        SyncOutboxEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoldGoDatabase : RoomDatabase() {
    abstract val orderDao: OrderDao
}
