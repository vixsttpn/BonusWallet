package com.bonuswallet.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY sortOrder ASC, id DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE cards SET sortOrder = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)
}
