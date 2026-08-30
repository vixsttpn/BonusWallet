
package com.bonuswallet.app.util

import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.domain.validator.CardInputValidator
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Serializable
data class BackupCard(
    val orgName: String,
    val title: String,
    val number: String,
    val format: String,
    val colorHex: String,
    val sortOrder: Int,
    val providerId: String = "generic",
    val cardName: String = "",
    val organizationName: String = "",
    val cardNumber: String = "",
    val barcodeValue: String = "",
    val barcodeType: String = "Автоматически"
)

@Serializable
data class BackupFile(
    val version: Int = 2,
    val app: String = "BonusWallet",
    val cards: List<BackupCard>
)

object BackupUtil {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun export(cards: List<CardEntity>): String {
        val backupCards = cards.map {
            BackupCard(
                orgName = it.getDisplayOrgName(),
                title = it.getDisplayTitle(),
                number = it.getDisplayNumber(),
                format = it.getDisplayFormat(),
                colorHex = it.colorHex,
                sortOrder = it.sortOrder,
                providerId = it.providerId,
                cardName = it.cardName,
                organizationName = it.organizationName,
                cardNumber = it.cardNumber,
                barcodeValue = it.barcodeValue,
                barcodeType = it.barcodeType
            )
        }
        return json.encodeToString(BackupFile(cards = backupCards))
    }

    /**
     * Import with validation pipeline - skip potential bank cards
     * Requirement 14: при импорте backup также проверять все карты через validation pipeline
     */
    fun import(jsonStr: String): List<CardEntity> {
        val file = json.decodeFromString<BackupFile>(jsonStr)
        return file.cards.mapNotNull { backup ->
            val numberToCheck = backup.cardNumber.ifBlank { backup.barcodeValue.ifBlank { backup.number } }
            val validation = CardInputValidator.validate(numberToCheck)
            // Skip high-risk bank cards
            if (validation.isPaymentCardHighRisk) {
                null // Не добавлять банковскую карту
            } else {
                CardEntity(
                    organizationName = backup.organizationName.takeIf { it.isNotBlank() } ?: backup.orgName.take(100),
                    orgName = backup.orgName.take(100),
                    cardName = backup.cardName.takeIf { it.isNotBlank() } ?: backup.title.take(100),
                    title = backup.title.take(100),
                    cardNumber = backup.cardNumber.takeIf { it.isNotBlank() } ?: backup.number.take(200),
                    barcodeValue = backup.barcodeValue.takeIf { it.isNotBlank() } ?: backup.number.take(200),
                    number = backup.number.take(200),
                    barcodeType = backup.barcodeType.takeIf { it.isNotBlank() } ?: backup.format,
                    format = if (backup.format in BarcodeUtil.SUPPORTED_FORMATS) backup.format else "Автоматически",
                    colorHex = backup.colorHex,
                    sortOrder = backup.sortOrder,
                    providerId = backup.providerId,
                    cardTheme = backup.providerId
                )
            }
        }
    }
}

