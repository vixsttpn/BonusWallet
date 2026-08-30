package com.bonuswallet.app.data

import androidx.compose.ui.graphics.Color

data class BrandConfig(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val textColor: Color,
    val bonusPercent: Int,
    val logoEmoji: String
)

object BrandRegistry {
    private val brands = listOf(
        BrandConfig("bravo_bonus", "Bravo Bonus %", Color(0xFF16A34A), Color(0xFF15803D), Color.White, 5, "❇️"),
        BrandConfig("bravo", "Bravo", Color(0xFF86EFAC), Color(0xFF4ADE80), Color(0xFF14532D), 5, "⬡"),
        BrandConfig("umico", "Umico", Color(0xFFFACC15), Color(0xFFFDE68A), Color(0xFF422006), 3, "🎁"),
        BrandConfig("umico_orange", "Umico", Color(0xFFFF8C00), Color(0xFFFFB347), Color.White, 3, "U"),
        BrandConfig("kfc", "KFC Bonus", Color(0xFFEF4444), Color(0xFFDC2626), Color.White, 7, "🍗"),
        BrandConfig("kfc_club", "KFC Club", Color(0xFFB91C1C), Color(0xFF7F1D1D), Color.White, 7, "🍗"),
        BrandConfig("client", "Client Discount", Color(0xFF7C3AED), Color(0xFF6D28D9), Color.White, 4, "%"),
        BrandConfig("default", "Bonus", Color(0xFF111827), Color(0xFF1F2937), Color.White, 2, "★")
    )

    fun resolve(orgName: String): BrandConfig {
        val l = orgName.lowercase()
        return when {
            "bravo" in l && ("karti" in l || l.length < 8) -> brands[1]
            "bravo" in l -> brands[0]
            "umico" in l && l.contains("bonus") -> brands[3]
            "umico" in l -> brands[2]
            "kfc" in l && "club" in l -> brands[5]
            "kfc" in l -> brands[4]
            "client" in l || "discount" in l -> brands[6]
            else -> brands[7].copy(displayName = orgName)
        }
    }
}
