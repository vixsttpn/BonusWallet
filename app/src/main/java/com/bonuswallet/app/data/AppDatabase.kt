
package com.bonuswallet.app.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(entities = [CardEntity::class, CardShowHistory::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE cards ADD COLUMN providerId TEXT NOT NULL DEFAULT 'generic'") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN cardName TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN organizationName TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN cardNumber TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN barcodeValue TEXT NOT NULL DEFAULT ''") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN barcodeType TEXT NOT NULL DEFAULT 'Автоматически'") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN cardTheme TEXT NOT NULL DEFAULT 'default'") } catch(e: Exception) {}
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("CREATE TABLE IF NOT EXISTS show_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, cardId INTEGER NOT NULL, timestamp INTEGER NOT NULL, latitude REAL, longitude REAL)") } catch(e: Exception) {}
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE cards ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN category TEXT NOT NULL DEFAULT 'Другое'") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN profileId TEXT NOT NULL DEFAULT 'mine'") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN photoUri TEXT") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN lastShownAt INTEGER") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN showCount INTEGER NOT NULL DEFAULT 0") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN notes TEXT") } catch(e: Exception) {}
                try { db.execSQL("ALTER TABLE cards ADD COLUMN expiryDate INTEGER") } catch(e: Exception) {}
                try { db.execSQL("CREATE TABLE IF NOT EXISTS show_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, cardId INTEGER NOT NULL, timestamp INTEGER NOT NULL, latitude REAL, longitude REAL)") } catch(e: Exception) {}
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "bonus_wallet.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

