
package com.bonuswallet.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "bonuswallet_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val THEME = stringPreferencesKey("theme") // light, dark, system
    }

    val termsAcceptedFlow: Flow<Boolean> = context.dataStore.data.map { it[TERMS_ACCEPTED] ?: false }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME] ?: "system" }

    suspend fun setTermsAccepted(accepted: Boolean) {
        context.dataStore.edit { it[TERMS_ACCEPTED] = accepted }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }
}
