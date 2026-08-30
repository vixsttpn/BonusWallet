
package com.bonuswallet.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Entity(tableName = "cards")
@Parcelize
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orgName: String,
    val title: String,
    val number: String,
    val format: String,
    val colorHex: String = "#111111",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
