
package com.bonuswallet.app.domain.provider

import com.bonuswallet.app.domain.detector.CardConfidence

/**
 * Bravo Supermarket Azerbaijan
 * ВАЖНО: У Bravo нет публичного официального API для проверки баланса сторонними приложениями.
 * Поэтому мы ЧЕСТНО возвращаем NOT_CHECKABLE и UNSUPPORTED, а не фейковый баланс.
 * В будущем если появится официальный API - можно реализовать.
 */
class BravoProvider : LoyaltyCardProvider {
    override val providerInfo = ProviderInfo(
        id = "bravo",
        displayName = "Bravo",
        description = "Bravo Supermarket - бонусная программа",
        supportsBalanceCheck = false, // Нет публичного API
        requiresAuth = true
    )

    override fun canHandle(cardNumber: String): CardConfidence {
        // Честно: у нас нет официального формата Bravo, поэтому максимум MEDIUM по эвристике
        val clean = cardNumber.replace("[^0-9]".toRegex(), "")
        // Типичные карты супермаркетов: 8-13 цифр, EAN
        return if (clean.length in 8..13 && clean.all { it.isDigit() }) {
            CardConfidence.MEDIUM
        } else if (cardNumber.length in 6..20) {
            CardConfidence.LOW
        } else {
            CardConfidence.UNKNOWN
        }
    }

    override fun validateFormat(cardNumber: String): Boolean {
        return cardNumber.length in 4..30
    }

    override suspend fun verifyCard(cardNumber: String): VerificationStatus {
        // Нет официального API для проверки существования карты Bravo извне
        // Мы не можем подтвердить карту, не нарушая условия сервиса
        return VerificationStatus.NOT_CHECKABLE
    }

    override suspend fun getBalance(cardNumber: String): BalanceResult {
        // Официального API нет, поэтому честно говорим что проверка недоступна
        // Не генерируем случайные 50 AZN или 100 бонусов
        return BalanceResult(
            status = BalanceStatus.UNSUPPORTED,
            message = "Для проверки баланса Bravo требуется вход в официальное приложение Bravo. BonusWallet не имеет доступа к официальному API Bravo."
        )
    }

    override fun getThemeId(): String = "bravo"
}

