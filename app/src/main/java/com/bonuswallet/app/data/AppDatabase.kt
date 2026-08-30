
package com.bonuswallet.app.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(entities = [CardEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cards ADD COLUMN providerId TEXT NOT NULL DEFAULT 'generic'")
                db.execSQL("ALTER TABLE cards ADD COLUMN cardName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN organizationName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN cardNumber TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN barcodeValue TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cards ADD COLUMN barcodeType TEXT NOT NULL DEFAULT 'Автоматически'")
                db.execSQL("ALTER TABLE cards ADD COLUMN cardTheme TEXT NOT NULL DEFAULT 'default'")
                db.execSQL("ALTER TABLE cards ADD COLUMN balance REAL")
                db.execSQL("ALTER TABLE cards ADD COLUMN balanceType TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN currency TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN bonusPoints INTEGER")
                db.execSQL("ALTER TABLE cards ADD COLUMN cashBalance REAL")
                db.execSQL("ALTER TABLE cards ADD COLUMN balanceAvailable INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cards ADD COLUMN balanceSource TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN status TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN lastBalanceUpdate INTEGER")
                db.execSQL("ALTER TABLE cards ADD COLUMN lastUsedAt INTEGER")
                db.execSQL("ALTER TABLE cards ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema change, just data normalization
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context, AppDatabase::class.java, "bonus_wallet.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

