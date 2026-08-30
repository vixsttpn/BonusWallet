
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

    @Query("SELECT * FROM cards WHERE cardNumber = :normalized OR barcodeValue = :normalized OR number = :normalized LIMIT 1")
    suspend fun findByNumber(normalized: String): CardEntity?

    @Query("SELECT * FROM cards WHERE organizationName LIKE '%' || :query || '%' OR orgName LIKE '%' || :query || '%' OR cardName LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR number LIKE '%' || :query || '%' OR providerId LIKE '%' || :query || '%' ORDER BY lastUsedAt DESC")
    fun searchFlow(query: String): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("UPDATE cards SET sortOrder = :order, updatedAt = :now WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE cards SET lastUsedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun updateLastUsed(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE cards SET balance = :balance, bonusPoints = :points, cashBalance = :cash, balanceAvailable = :available, balanceSource = :source, lastBalanceUpdate = :updateTime, updatedAt = :now WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double?, points: Int?, cash: Double?, available: Boolean, source: String?, updateTime: Long?, now: Long = System.currentTimeMillis())

    @Query("SELECT MAX(sortOrder) FROM cards")
    suspend fun getMaxOrder(): Int?

    @Query("SELECT * FROM cards ORDER BY lastUsedAt DESC")
    fun getByLastUsedFlow(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY organizationName ASC, orgName ASC")
    fun getByNameAscFlow(): Flow<List<CardEntity>>
}

