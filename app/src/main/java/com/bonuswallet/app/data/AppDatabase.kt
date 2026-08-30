package com.bonuswallet.app.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [CardEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "bonus_wallet.db")
                   .fallbackToDestructiveMigration()
                   .build().also { INSTANCE = it }
            }
        }
    }
}
