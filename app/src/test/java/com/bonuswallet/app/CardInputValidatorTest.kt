
package com.bonuswallet.app

import com.bonuswallet.app.domain.validator.CardInputValidator
import com.bonuswallet.app.domain.validator.ValidationStatus
import org.junit.Assert.*
import org.junit.Test

class CardInputValidatorTest {

    @Test
    fun testValidBonusCard() {
        val result = CardInputValidator.validate("1234567890")
        assertTrue(result.canSave)
        assertNotEquals(ValidationStatus.PAYMENT_CARD_DETECTED, result.status)
    }

    @Test
    fun testBankCardBlocked() {
        val visa = "4111111111111111"
        val result = CardInputValidator.validate(visa)
        assertEquals(ValidationStatus.PAYMENT_CARD_DETECTED, result.status)
        assertFalse(result.canSave)
        assertTrue(result.isPaymentCardHighRisk)
    }

    @Test
    fun testEmpty() {
        val result = CardInputValidator.validate("")
        assertFalse(result.canSave)
    }

    @Test
    fun testTooLong() {
        val long = "1".repeat(201)
        val result = CardInputValidator.validate(long)
        assertFalse(result.canSave)
    }

    @Test
    fun testDuplicateLogic() {
        // Duplicate check is done in DAO, but validator should allow format
        val result = CardInputValidator.validate("12345678")
        assertTrue(result.canSave)
    }

    @Test
    fun testUnknownNumber() {
        val result = CardInputValidator.validate("XYZ-123")
        assertTrue(result.canSave) // Code128 allows letters
    }

    @Test
    fun testCvvField() {
        val result = CardInputValidator.validateForCvvField("123")
        assertEquals(ValidationStatus.CVV_DETECTED, result.status)
        assertFalse(result.canSave)
    }
}

