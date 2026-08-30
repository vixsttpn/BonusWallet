
package com.bonuswallet.app

import com.bonuswallet.app.util.BackupUtil
import org.junit.Assert.*
import org.junit.Test

class BackupImportTest {

    @Test
    fun testImportWithBankCardFiltered() {
        val json = """
        {
            "version": 2,
            "app": "BonusWallet",
            "cards": [
                {"orgName": "Bravo", "title": "Bravo", "number": "12345678", "format": "Code 128", "colorHex": "#111111", "sortOrder": 0},
                {"orgName": "Visa Card", "title": "Bank", "number": "4111111111111111", "format": "Code 128", "colorHex": "#111111", "sortOrder": 1}
            ]
        }
        """.trimIndent()
        val imported = BackupUtil.import(json)
        // Bank card should be filtered out
        assertEquals(1, imported.size)
        assertEquals("Bravo", imported[0].orgName)
    }

    @Test
    fun testExportImportRoundTrip() {
        val json = """
        {
            "version": 2,
            "app": "BonusWallet",
            "cards": [
                {"orgName": "Bravo", "title": "Bravo", "number": "12345678", "format": "Code 128", "colorHex": "#111111", "sortOrder": 0}
            ]
        }
        """.trimIndent()
        val imported = BackupUtil.import(json)
        assertEquals(1, imported.size)
        val exported = BackupUtil.export(imported)
        assertTrue(exported.contains("Bravo"))
    }
}

