
package com.bonuswallet.app.domain.provider

import com.bonuswallet.app.domain.detector.CardConfidence

object ProviderRegistry {
    private val providers: List<LoyaltyCardProvider> = listOf(
        BravoProvider(),
        WoltProvider(),
        KfcProvider(),
        GenericCardProvider()
    )

    fun getAllProviders(): List<LoyaltyCardProvider> = providers

    fun getProviderById(id: String): LoyaltyCardProvider {
        return providers.find { it.providerInfo.id == id } ?: GenericCardProvider()
    }

    fun getPopularProviders(): List<LoyaltyCardProvider> {
        return listOf(
            getProviderById("bravo"),
            getProviderById("wolt"),
            getProviderById("kfc"),
            getProviderById("generic")
        )
    }

    fun detectBestProvider(cardNumber: String): Pair<LoyaltyCardProvider, CardConfidence> {
        var bestProvider: LoyaltyCardProvider = GenericCardProvider()
        var bestConfidence = CardConfidence.UNKNOWN

        for (provider in providers) {
            val confidence = provider.canHandle(cardNumber)
            if (confidence.ordinal > bestConfidence.ordinal) {
                bestConfidence = confidence
                bestProvider = provider
            }
        }

        return bestProvider to bestConfidence
    }

    fun searchProviders(query: String): List<LoyaltyCardProvider> {
        if (query.isBlank()) return getPopularProviders()
        val lower = query.lowercase()
        return providers.filter {
            it.providerInfo.displayName.lowercase().contains(lower) ||
            it.providerInfo.id.lowercase().contains(lower)
        }
    }
}

