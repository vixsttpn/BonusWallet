package com.bonuswallet.app.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "", val orgName: String = "", val organizationName: String = "",
    val cardName: String = "", val number: String = "", val cardNumber: String = "",
    val barcodeValue: String = "", val barcodeType: String = "Автоматически",
    val format: String = "Автоматически", val colorHex: String = "#111111",
    val providerId: String = "generic", val cardTheme: String = "default",
    val status: String? = null, val sortOrder: Int = 0,
    val balance: Double? = null, val balanceType: String? = null,
    val currency: String? = null, val bonusPoints: Int? = null,
    val cashBalance: Double? = null, val balanceAvailable: Boolean = false,
    val balanceSource: String? = null, val lastBalanceUpdate: Long? = null,
    val lastUsedAt: Long? = null, val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(), val isFavorite: Boolean = false,
    val category: String = "Другое", val profileId: String = "mine",
    val photoUri: String? = null, val lastShownAt: Long? = null,
    val showCount: Int = 0, val notes: String? = null, val expiryDate: Long? = null
) {
    fun getDisplayTitle(): String = when { title.isNotBlank() -> title; cardName.isNotBlank() -> cardName; organizationName.isNotBlank() -> organizationName; orgName.isNotBlank() -> orgName; else -> "Карта" }
    fun getDisplayOrgName(): String = when { organizationName.isNotBlank() -> organizationName; orgName.isNotBlank() -> orgName; title.isNotBlank() -> title; cardName.isNotBlank() -> cardName; else -> "Магазин" }
    fun getDisplayNumber(): String = when { barcodeValue.isNotBlank() -> barcodeValue; cardNumber.isNotBlank() -> cardNumber; number.isNotBlank() -> number; else -> "" }
    fun isBalanceReal(): Boolean = balanceAvailable && (balance != null || bonusPoints != null || cashBalance != null)
    fun getDisplayFormat(): String = when { barcodeType.isNotBlank() && barcodeType != "Автоматически" -> barcodeType; format.isNotBlank() -> format; else -> "Автоматически" }
}
@Entity(tableName = "show_history")
data class CardShowHistory(@PrimaryKey(autoGenerate = true) val id: Long = 0, val cardId: Long, val timestamp: Long = System.currentTimeMillis(), val latitude: Double? = null, val longitude: Double? = null)
