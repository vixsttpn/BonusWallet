
package com.bonuswallet.app.domain.provider

import com.bonuswallet.app.domain.detector.CardConfidence

/**
 * Расширяемая архитектура провайдеров.
 * Каждый provider отвечает за определение, проверку, получение баланса.
 * Если нет реального API - возвращает unsupported, а не фейк.
 */

enum class VerificationStatus {
    VERIFIED,           // Карта подтверждена реальным способом
    FORMAT_VALID,       // Формат проверен
    TYPE_DETECTED,      // Тип определен
    NOT_CHECKABLE,      // Проверка недоступна (нет официального API)
    INVALID,            // Неверный номер
    NOT_FOUND           // Не удалось подтвердить существование
}

enum class BalanceStatus {
    SUCCESS,
    UNSUPPORTED,        // Программа не предоставляет баланс через доступный способ
    REQUIRES_AUTH,      // Требуется вход в аккаунт
    NETWORK_ERROR,      // Сервер недоступен
    INVALID_NUMBER,     // Неправильный номер
    NOT_CONFIRMED,      // Не удалось подтвердить
    NOT_AVAILABLE       // Баланс недоступен
}

data class BalanceResult(
    val status: BalanceStatus,
    val balance: Double? = null,
    val bonusPoints: Int? = null,
    val cashBalance: Double? = null,
    val currency: String? = null,
    val message: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ProviderInfo(
    val id: String,
    val displayName: String,
    val description: String,
    val supportsBalanceCheck: Boolean,
    val requiresAuth: Boolean
)

interface LoyaltyCardProvider {
    val providerInfo: ProviderInfo

    /**
     * Может ли этот провайдер обработать данный номер?
     * Возвращает confidence.
     */
    fun canHandle(cardNumber: String): CardConfidence

    /**
     * Проверка формата номера для этого провайдера
     */
    fun validateFormat(cardNumber: String): Boolean

    /**
     * Попытка проверить существование карты.
     * Если нет официального API - возвращает NOT_CHECKABLE честно.
     */
    suspend fun verifyCard(cardNumber: String): VerificationStatus

    /**
     * Попытка получить баланс.
     * Если нет официального API - возвращает UNSUPPORTED, а не фейк.
     */
    suspend fun getBalance(cardNumber: String): BalanceResult

    /**
     * Тема карты
     */
    fun getThemeId(): String
}

