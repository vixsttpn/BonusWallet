
package com.bonuswallet.app.domain.provider

import com.bonuswallet.app.domain.detector.CardConfidence

/**
 * Wolt
 * Wolt - сервис доставки, у него нет физических бонусных карт с номером в традиционном понимании.
 * Баланс Wolt - это кредиты в аккаунте, требуют авторизации через официальный API Wolt (который не публичный для сторонних кошельков).
 * Поэтому честно возвращаем REQUIRES_AUTH / UNSUPPORTED.
 */
class WoltProvider : LoyaltyCardProvider {
    override val providerInfo = ProviderInfo(
        id = "wolt",
        displayName = "Wolt",
        description = "Wolt - кредиты и бонусы",
        supportsBalanceCheck = false,
        requiresAuth = true
    )

    override fun canHandle(cardNumber: String): CardConfidence {
        // Wolt обычно использует QR или ID аккаунта, не типичный штрих-код карты
        return if (cardNumber.length in 6..40) CardConfidence.LOW else CardConfidence.UNKNOWN
    }

    override fun validateFormat(cardNumber: String): Boolean {
        return cardNumber.length in 4..100
    }

    override suspend fun verifyCard(cardNumber: String): VerificationStatus {
        return VerificationStatus.NOT_CHECKABLE
    }

    override suspend fun getBalance(cardNumber: String): BalanceResult {
        return BalanceResult(
            status = BalanceStatus.REQUIRES_AUTH,
            message = "Для проверки баланса Wolt требуется вход в аккаунт Wolt. Эта программа не предоставляет баланс через доступный способ проверки без авторизации."
        )
    }

    override fun getThemeId(): String = "wolt"
}

