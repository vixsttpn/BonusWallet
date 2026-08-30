
package com.bonuswallet.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllFlow(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY sortOrder ASC")
    suspend fun getAll(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("UPDATE cards SET sortOrder = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)

    @Query("SELECT MAX(sortOrder) FROM cards")
    suspend fun getMaxOrder(): Int?
}
