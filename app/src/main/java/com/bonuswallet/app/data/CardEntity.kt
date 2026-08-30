
package com.bonuswallet.app.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Полная локальная модель карты.
 * Все поля nullable там где данные могут отсутствовать - требование 28.
 * Никогда не генерируем фейковый баланс.
 */
@Entity(tableName = "cards")
@Parcelize
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Provider system
    val providerId: String = "generic", // bravo, wolt, kfc, generic, other

    // Names
    val cardName: String = "",
    val organizationName: String = "",
    
    // Legacy fields for backward compatibility
    val orgName: String = "",
    val title: String = "",

    // Number / Barcode
    val cardNumber: String = "",
    val barcodeValue: String = "",
    val barcodeType: String = "Автоматически",
    val number: String = "", // legacy
    val format: String = "Автоматически", // legacy

    // Theme
    val cardTheme: String = "default",
    val colorHex: String = "#111111",

    // Balance model - requirement 6, 28
    val balance: Double? = null,
    val balanceType: String? = null, // BONUS, CASH, COMBINED, UNAVAILABLE
    val currency: String? = null, // AZN, USD, etc
    val bonusPoints: Int? = null,
    val cashBalance: Double? = null,
    val balanceAvailable: Boolean = false,
    val balanceSource: String? = null, // REAL_API, MANUAL, NONE
    val status: String? = null, // ACTIVE, INACTIVE, UNKNOWN
    val lastBalanceUpdate: Long? = null,

    // Timestamps
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sorting
    val sortOrder: Int = 0
) : Parcelable {

    fun getDisplayOrgName(): String = organizationName.ifBlank { orgName.ifBlank { cardName } }
    fun getDisplayTitle(): String = cardName.ifBlank { title.ifBlank { getDisplayOrgName() } }
    fun getDisplayNumber(): String = cardNumber.ifBlank { barcodeValue.ifBlank { number } }
    fun getDisplayFormat(): String = barcodeType.ifBlank { format }

    fun isBalanceReal(): Boolean = balanceAvailable && balanceSource == "REAL_API" && (balance != null || bonusPoints != null)
}

