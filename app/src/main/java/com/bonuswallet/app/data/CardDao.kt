
package com.bonuswallet.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY isFavorite DESC, showCount DESC, sortOrder ASC, updatedAt DESC")
    fun getAllFlow(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY isFavorite DESC, sortOrder ASC")
    suspend fun getAll(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE cardNumber = :number OR barcodeValue = :number OR number = :number LIMIT 1")
    suspend fun findDuplicate(number: String): CardEntity?

    @Query("SELECT * FROM cards WHERE isFavorite = 1 ORDER BY lastShownAt DESC")
    fun getFavoritesFlow(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE category = :category ORDER BY sortOrder ASC")
    fun getByCategoryFlow(category: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE profileId = :profileId ORDER BY sortOrder ASC")
    fun getByProfileFlow(profileId: String): Flow<List<CardEntity>>

    @Query("SELECT COUNT(*) FROM cards WHERE cardNumber = :number OR barcodeValue = :number")
    suspend fun countByNumber(number: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("SELECT MAX(sortOrder) FROM cards")
    suspend fun getMaxOrder(): Int?

    @Query("UPDATE cards SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE cards SET lastShownAt = :time, showCount = showCount + 1, lastUsedAt = :time WHERE id = :id")
    suspend fun markShown(id: Long, time: Long)

    // History
    @Insert
    suspend fun insertHistory(history: CardShowHistory)

    @Query("SELECT * FROM show_history WHERE cardId = :cardId ORDER BY timestamp DESC LIMIT 50")
    fun getHistoryFlow(cardId: Long): Flow<List<CardShowHistory>>

    @Query("SELECT * FROM show_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistoryFlow(): Flow<List<CardShowHistory>>
}

