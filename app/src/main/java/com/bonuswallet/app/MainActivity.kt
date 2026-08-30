
package com.bonuswallet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.data.PreferencesManager
import com.bonuswallet.app.ui.screens.*
import com.bonuswallet.app.ui.theme.BonusWalletTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        val db = (application as BonusWalletApp).database

        setContent {
            var theme by remember { mutableStateOf("system") }
            var termsAccepted by remember { mutableStateOf<Boolean?>(null) }
            var cards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }

            LaunchedEffect(Unit) {
                theme = prefs.themeFlow.first()
                termsAccepted = prefs.termsAcceptedFlow.first()
                db.cardDao().getAllFlow().collect { cards = it }
            }

            BonusWalletTheme(themeChoice = theme) {
                if (termsAccepted == null) {
                    Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
                    return@BonusWalletTheme
                }

                val navController = rememberNavController()
                val start = if (termsAccepted == true) "home" else "terms"

                NavHost(navController = navController, startDestination = start) {
                    composable("terms") {
                        TermsScreen(onAccepted = {
                            lifecycleScope.launch {
                                prefs.setTermsAccepted(true)
                                termsAccepted = true
                                navController.navigate("home") { popUpTo("terms"){ inclusive = true } }
                            }
                        })
                    }
                    composable("home") {
                        var reorderList by remember { mutableStateOf<List<CardEntity>?>(null) }
                        HomeScreen(
                            cards = cards,
                            onCardClick = { id -> navController.navigate("detail/$id") },
                            onAddClick = { navController.navigate("add") },
                            onSettingsClick = { navController.navigate("settings") },
                            onReorder = {}
                        )
                    }
                    composable("add") {
                        AddEditCardScreen(existing = null, onSave = { entity ->
                            lifecycleScope.launch {
                                val maxOrder = db.cardDao().getMaxOrder() ?: 0
                                db.cardDao().insert(entity.copy(sortOrder = maxOrder + 1))
                                navController.popBackStack()
                            }
                        }, onBack = { navController.popBackStack() })
                    }
                    composable("edit/{id}") { backStack ->
                        val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        val existing = cards.find { it.id == id }
                        if (existing != null) {
                            AddEditCardScreen(existing = existing, onSave = { entity ->
                                lifecycleScope.launch {
                                    db.cardDao().update(entity)
                                    navController.popBackStack()
                                }
                            }, onBack = { navController.popBackStack() })
                        }
                    }
                    composable("detail/{id}") { backStack ->
                        val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        val card = cards.find { it.id == id }
                        if (card != null) {
                            CardDetailScreen(
                                card = card,
                                onEdit = { navController.navigate("edit/$id") },
                                onDelete = {
                                    lifecycleScope.launch {
                                        db.cardDao().delete(card)
                                        navController.popBackStack()
                                    }
                                },
                                onFullscreen = { navController.navigate("fullscreen/$id") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable("fullscreen/{id}") { backStack ->
                        val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        val card = cards.find { it.id == id }
                        if (card != null) {
                            FullscreenBarcodeScreen(card = card, onClose = { navController.popBackStack() })
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            currentTheme = theme,
                            onThemeChange = { newTheme ->
                                theme = newTheme
                                lifecycleScope.launch { prefs.setTheme(newTheme) }
                            },
                            cards = cards,
                            onImport = { imported ->
                                lifecycleScope.launch {
                                    val currentMax = db.cardDao().getMaxOrder() ?: 0
                                    imported.forEachIndexed { idx, c ->
                                        db.cardDao().insert(c.copy(id = 0, sortOrder = currentMax + idx + 1))
                                    }
                                }
                            },
                            onDeleteAll = {
                                lifecycleScope.launch { db.cardDao().deleteAll() }
                            },
                            onPrivacy = { navController.navigate("privacy") },
                            onTerms = { navController.navigate("terms_view") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("privacy") { PrivacyScreen(onBack = { navController.popBackStack() }) }
                    composable("terms_view") { TermsViewScreen(onBack = { navController.popBackStack() }) }
                }
            }
        }
    }
}
