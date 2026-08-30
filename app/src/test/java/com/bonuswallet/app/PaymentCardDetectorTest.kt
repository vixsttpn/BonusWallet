
package com.bonuswallet.app

import com.bonuswallet.app.domain.detector.PaymentCardDetector
import com.bonuswallet.app.domain.detector.PaymentCardConfidence
import org.junit.Assert.*
import org.junit.Test

class PaymentCardDetectorTest {

    @Test
    fun testValidVisa() {
        // Real Visa test number (not real card, test PAN from docs)
        val visa = "4111111111111111"
        val result = PaymentCardDetector.detect(visa)
        assertTrue(result.isPotentialPaymentCard)
        assertEquals(PaymentCardConfidence.HIGH, result.confidence)
        assertTrue(result.luhnValid)
    }

    @Test
    fun testValidMastercard() {
        val mc = "5555555555554444"
        val result = PaymentCardDetector.detect(mc)
        assertTrue(result.isPotentialPaymentCard)
        assertEquals(PaymentCardConfidence.HIGH, result.confidence)
    }

    @Test
    fun testValidAmex() {
        val amex = "378282246310005"
        val result = PaymentCardDetector.detect(amex)
        assertTrue(result.isPotentialPaymentCard)
        assertEquals(PaymentCardConfidence.HIGH, result.confidence)
    }

    @Test
    fun testUnionPay() {
        val union = "6200000000000005"
        val result = PaymentCardDetector.detect(union)
        assertTrue(result.isPotentialPaymentCard)
    }

    @Test
    fun testLuhnPositiveRandomNotBankCard() {
        // Random number that passes Luhn but not payment network - should NOT be high risk
        // Example: 1234567812345670 passes Luhn? Let's use known Luhn valid but not in 13-19 payment range pattern
        val randomLuhn = "12345678903" // 11 digits, passes Luhn but too short for PAN
        val result = PaymentCardDetector.detect(randomLuhn)
        assertFalse(PaymentCardDetector.isHighRiskBankCard(randomLuhn))
    }

    @Test
    fun testBonusCardNotBankCard() {
        // Typical bonus card: 8 digits
        val bonus = "12345678"
        val result = PaymentCardDetector.detect(bonus)
        assertFalse(result.isPotentialPaymentCard)
        assertEquals(PaymentCardConfidence.NOT_PAYMENT, result.confidence)
    }

    @Test
    fun testUnknownNumber() {
        val unknown = "ABC123"
        val result = PaymentCardDetector.detect(unknown)
        assertFalse(result.isPotentialPaymentCard)
    }

    @Test
    fun testEmpty() {
        val result = PaymentCardDetector.detect("")
        assertFalse(result.isPotentialPaymentCard)
    }

    @Test
    fun testTooLong() {
        val long = "1".repeat(201)
        val result = PaymentCardDetector.detect(long)
        assertFalse(result.isPotentialPaymentCard)
    }

    @Test
    fun testCvvLength() {
        // 3 digits should not be considered PAN
        val cvv = "123"
        val result = PaymentCardDetector.detect(cvv)
        assertFalse(result.isPotentialPaymentCard)
    }

    @Test
    fun testLuhnAlgorithm() {
        assertTrue(PaymentCardDetector.isLuhnValid("4111111111111111"))
        assertFalse(PaymentCardDetector.isLuhnValid("4111111111111112"))
    }
}

