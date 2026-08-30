
package com.bonuswallet.app.domain.theme

import androidx.compose.ui.graphics.Color

data class CardTheme(
    val id: String,
    val background: Color,
    val secondaryBackground: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val accentColor: Color,
    val icon: String, // Neutral icon, not trademarked logo
    val layoutStyle: String = "modern"
)

object CardThemeResolver {
    private val themes = mapOf(
        "bravo" to CardTheme(
            id = "bravo",
            background = Color(0xFF1B8A4D), // Inspired but not copying exact official
            secondaryBackground = Color(0xFF146E3D),
            textColor = Color.White,
            secondaryTextColor = Color(0xFFE8F5E9),
            accentColor = Color(0xFF81C784),
            icon = "🛒",
            layoutStyle = "retail"
        ),
        "wolt" to CardTheme(
            id = "wolt",
            background = Color(0xFF00B4D8),
            secondaryBackground = Color(0xFF0096C7),
            textColor = Color.White,
            secondaryTextColor = Color(0xFFCAF0F8),
            accentColor = Color(0xFF90E0EF),
            icon = "🛵",
            layoutStyle = "delivery"
        ),
        "kfc" to CardTheme(
            id = "kfc",
            background = Color(0xFFD32F2F),
            secondaryBackground = Color(0xFFB71C1C),
            textColor = Color.White,
            secondaryTextColor = Color(0xFFFFCDD2),
            accentColor = Color(0xFFEF9A9A),
            icon = "🍗",
            layoutStyle = "food"
        ),
        "generic" to CardTheme(
            id = "generic",
            background = Color(0xFF111827),
            secondaryBackground = Color(0xFF1F2937),
            textColor = Color.White,
            secondaryTextColor = Color(0xFF9CA3AF),
            accentColor = Color(0xFF6B7280),
            icon = "★",
            layoutStyle = "default"
        ),
        "default" to CardTheme(
            id = "default",
            background = Color(0xFF1F2937),
            secondaryBackground = Color(0xFF111827),
            textColor = Color.White,
            secondaryTextColor = Color(0xFF9CA3AF),
            accentColor = Color(0xFF4B5563),
            icon = "💳",
            layoutStyle = "default"
        )
    )

    fun resolve(providerId: String): CardTheme {
        return themes[providerId] ?: themes["default"]!!
    }

    fun resolveByOrgName(orgName: String): CardTheme {
        val lower = orgName.lowercase()
        return when {
            "bravo" in lower -> resolve("bravo")
            "wolt" in lower -> resolve("wolt")
            "kfc" in lower -> resolve("kfc")
            else -> resolve("generic")
        }
    }

    fun getAllThemes(): List<CardTheme> = themes.values.toList()
}

object CardTypeFormatter {
    fun formatBalance(balanceResult: com.bonuswallet.app.domain.provider.BalanceResult): String {
        return when (balanceResult.status) {
            com.bonuswallet.app.domain.provider.BalanceStatus.SUCCESS -> {
                when {
                    balanceResult.bonusPoints != null && balanceResult.cashBalance != null -> "${balanceResult.bonusPoints} бонусов\n${String.format("%.2f", balanceResult.cashBalance)} ${balanceResult.currency ?: "₼"}"
                    balanceResult.bonusPoints != null -> "${balanceResult.bonusPoints} бонусов"
                    balanceResult.cashBalance != null -> String.format("%.2f ${balanceResult.currency ?: "₼"}", balanceResult.cashBalance)
                    balanceResult.balance != null -> String.format("%.2f ${balanceResult.currency ?: ""}", balanceResult.balance)
                    else -> "Баланс недоступен"
                }
            }
            com.bonuswallet.app.domain.provider.BalanceStatus.UNSUPPORTED -> "Баланс недоступен"
            com.bonuswallet.app.domain.provider.BalanceStatus.REQUIRES_AUTH -> "Для проверки баланса требуется вход в аккаунт"
            com.bonuswallet.app.domain.provider.BalanceStatus.NETWORK_ERROR -> "Не удалось получить актуальный баланс"
            com.bonuswallet.app.domain.provider.BalanceStatus.INVALID_NUMBER -> "Проверьте номер карты"
            com.bonuswallet.app.domain.provider.BalanceStatus.NOT_CONFIRMED -> "Не удалось подтвердить существование карты"
            com.bonuswallet.app.domain.provider.BalanceStatus.NOT_AVAILABLE -> "Баланс пока недоступен"
        }
    }
}

