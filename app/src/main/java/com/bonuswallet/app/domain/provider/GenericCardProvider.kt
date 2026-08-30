
package com.bonuswallet.app.domain.provider

import com.bonuswallet.app.domain.detector.CardConfidence

class GenericCardProvider : LoyaltyCardProvider {
    override val providerInfo = ProviderInfo(
        id = "generic",
        displayName = "Бонусная карта",
        description = "Универсальная бонусная карта",
        supportsBalanceCheck = false,
        requiresAuth = false
    )

    override fun canHandle(cardNumber: String): CardConfidence {
        return if (cardNumber.length in 4..30) CardConfidence.LOW else CardConfidence.UNKNOWN
    }

    override fun validateFormat(cardNumber: String): Boolean {
        return cardNumber.length in 4..200
    }

    override suspend fun verifyCard(cardNumber: String): VerificationStatus {
        // Нет официального способа проверить generic карту
        return VerificationStatus.NOT_CHECKABLE
    }

    override suspend fun getBalance(cardNumber: String): BalanceResult {
        // Нет официального API для generic карт
        return BalanceResult(
            status = BalanceStatus.UNSUPPORTED,
            message = "Эта программа не предоставляет баланс через доступный способ проверки"
        )
    }

    override fun getThemeId(): String = "default"
}

