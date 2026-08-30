
package com.bonuswallet.app.domain.provider

import com.bonuswallet.app.domain.detector.CardConfidence

/**
 * KFC Club / KFC Bonus
 * KFC использует свою программу лояльности через официальное приложение.
 * Публичного API для сторонних кошельков нет.
 */
class KfcProvider : LoyaltyCardProvider {
    override val providerInfo = ProviderInfo(
        id = "kfc",
        displayName = "KFC",
        description = "KFC Club - бонусная программа",
        supportsBalanceCheck = false,
        requiresAuth = true
    )

    override fun canHandle(cardNumber: String): CardConfidence {
        val clean = cardNumber.replace("[^0-9]".toRegex(), "")
        return if (clean.length in 6..20) CardConfidence.LOW else CardConfidence.UNKNOWN
    }

    override fun validateFormat(cardNumber: String): Boolean {
        return cardNumber.length in 4..50
    }

    override suspend fun verifyCard(cardNumber: String): VerificationStatus {
        return VerificationStatus.NOT_CHECKABLE
    }

    override suspend fun getBalance(cardNumber: String): BalanceResult {
        return BalanceResult(
            status = BalanceStatus.UNSUPPORTED,
            message = "Эта программа не предоставляет баланс через доступный способ проверки. Проверьте баланс в официальном приложении KFC."
        )
    }

    override fun getThemeId(): String = "kfc"
}

