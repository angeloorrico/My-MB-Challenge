package com.aorrico.mymbchallenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aorrico.mymbchallenge.data.local.dao.RecentlyViewedExchangeDao
import com.aorrico.mymbchallenge.data.local.entity.RecentlyViewedExchangeEntity

@Database(entities = [RecentlyViewedExchangeEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentlyViewedExchangeDao(): RecentlyViewedExchangeDao
}
