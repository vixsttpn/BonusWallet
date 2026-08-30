
package com.bonuswallet.app

import android.app.Application
import androidx.room.Room
import com.bonuswallet.app.data.AppDatabase

class BonusWalletApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "bonuswallet_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
