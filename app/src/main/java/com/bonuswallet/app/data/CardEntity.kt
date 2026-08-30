package com.bonuswallet.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgName: String,
    val title: String,
    val number: String,
    val format: String,
    val colorHex: String,
    val sortOrder: Int,
    // === НОВОЕ: Бонусы как на скринах ===
    val bonusBalance: Int = (85..1240).random(),
    val bonusPercent: Int = 0,
    val level: String = "Standard",
    val nextLevelPoints: Int = 500,
    val isFavorite: Boolean = false,
    val cashbackRate: String = "5%"
)
