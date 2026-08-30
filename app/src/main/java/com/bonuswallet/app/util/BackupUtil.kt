
package com.bonuswallet.app.util

import com.bonuswallet.app.data.CardEntity
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
    val sortOrder: Int
)

@Serializable
data class BackupFile(
    val version: Int = 1,
    val app: String = "BonusWallet",
    val cards: List<BackupCard>
)

object BackupUtil {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun export(cards: List<CardEntity>): String {
        val backupCards = cards.map {
            BackupCard(it.orgName, it.title, it.number, it.format, it.colorHex, it.sortOrder)
        }
        return json.encodeToString(BackupFile(cards = backupCards))
    }

    fun import(jsonStr: String): List<CardEntity> {
        val file = json.decodeFromString<BackupFile>(jsonStr)
        return file.cards.map {
            CardEntity(
                orgName = it.orgName.take(100),
                title = it.title.take(100),
                number = it.number.take(200),
                format = if (it.format in com.bonuswallet.app.util.BarcodeUtil.SUPPORTED_FORMATS) it.format else "Автоматически",
                colorHex = it.colorHex,
                sortOrder = it.sortOrder
            )
        }
    }
}
