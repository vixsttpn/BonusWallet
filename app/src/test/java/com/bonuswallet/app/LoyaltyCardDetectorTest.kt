
package com.bonuswallet.app

import com.bonuswallet.app.domain.detector.BarcodeType
import com.bonuswallet.app.domain.detector.CardConfidence
import com.bonuswallet.app.domain.detector.LoyaltyCardDetector
import org.junit.Assert.*
import org.junit.Test

class LoyaltyCardDetectorTest {

    @Test
    fun testEanDetection() {
        val result = LoyaltyCardDetector.detect("1234567890123", BarcodeType.EAN_13)
        assertTrue(result.allMatches.isNotEmpty())
        // Should be at most MEDIUM because no official format
        assertTrue(result.bestMatch?.confidence != CardConfidence.HIGH || result.bestMatch?.providerId == "generic")
    }

    @Test
    fun testQrDetection() {
        val result = LoyaltyCardDetector.detect("https://example.com/card/123", BarcodeType.QR_CODE)
        assertTrue(result.allMatches.any { it.providerId == "wolt" || it.providerId == "kfc" })
    }

    @Test
    fun testManualSelectionHighConfidence() {
        val detection = LoyaltyCardDetector.detectByOrganizationName("Bravo Supermarket")
        assertEquals(CardConfidence.HIGH, detection.confidence)
        assertEquals("bravo", detection.providerId)
    }

    @Test
    fun testEmpty() {
        val result = LoyaltyCardDetector.detect("", BarcodeType.UNKNOWN)
        assertFalse(result.isDeterminable)
    }
}

