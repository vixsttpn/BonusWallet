
package com.bonuswallet.app.util

import com.bonuswallet.app.domain.provider.BalanceResult
import com.bonuswallet.app.domain.provider.BalanceStatus
import java.text.SimpleDateFormat
import java.util.*

object BalanceFormatter {
    fun formatLastUpdated(timestamp: Long?): String {
        if (timestamp == null) return "Никогда не обновлялось"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Обновлено только что"
            diff < 3600_000 -> "Обновлено ${diff / 60000} мин назад"
            diff < 86400_000 -> {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Обновлено сегодня, ${sdf.format(Date(timestamp))}"
            }
            diff < 172800_000 -> "Обновлено вчера"
            else -> {
                val days = diff / 86400_000
                "Последняя проверка: $days дн назад"
            }
        }
    }

    fun formatBalanceForCard(balance: Double?, bonusPoints: Int?, cashBalance: Double?, currency: String?, available: Boolean): String {
        if (!available) return "Баланс недоступен"
        return when {
            bonusPoints != null && cashBalance != null -> "$bonusPoints бонусов • ${String.format("%.2f %s", cashBalance, currency ?: "₼")}"
            bonusPoints != null -> "$bonusPoints бонусов"
            cashBalance != null -> String.format("%.2f %s", cashBalance, currency ?: "₼")
            balance != null -> String.format("%.2f %s", balance, currency ?: "")
            else -> "Баланс недоступен"
        }
    }
}

