package com.bonuswallet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bonuswallet.app.data.AppDatabase
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.ui.screens.*
import com.bonuswallet.app.ui.theme.BonusWalletTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(this)
        val dao = db.cardDao()

        setContent {
            BonusWalletTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    var cards by remember { mutableStateOf<List<CardEntity>>(emptyList()) }

                    LaunchedEffect(Unit) {
                        dao.getAllCards().collectLatest { list ->
                            cards = list
                        }
                    }

                    NavHost(navController = nav, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                cards = cards,
                                onCardClick = { card ->
                                    nav.navigate("detail/${card.id}")
                                },
                                onAdd = {
                                    nav.navigate("add")
                                }
                            )
                        }
                        composable("add") {
                            AddEditCardScreen(
                                existing = null,
                                onSave = { entity ->
                                    lifecycleScope.launch {
                                        val maxOrder = cards.maxOfOrNull { it.sortOrder } ?: 0
                                        dao.insert(entity.copy(sortOrder = maxOrder + 1))
                                        nav.popBackStack()
                                    }
                                },
                                onBack = { nav.popBackStack() }
                            )
                        }
                        composable("detail/{id}") { backStack ->
                            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            val card = cards.find { it.id == id }
                            if (card != null) {
                                CardDetailScreen(
                                    card = card,
                                    onEdit = { nav.navigate("edit/$id") },
                                    onDelete = {
                                        lifecycleScope.launch {
                                            dao.delete(card)
                                            nav.popBackStack()
                                        }
                                    },
                                    onFullscreen = { nav.navigate("fullscreen/$id") },
                                    onBack = { nav.popBackStack() }
                                )
                            }
                        }
                        composable("edit/{id}") { backStack ->
                            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            val card = cards.find { it.id == id }
                            if (card != null) {
                                AddEditCardScreen(
                                    existing = card,
                                    onSave = { entity ->
                                        lifecycleScope.launch {
                                            dao.update(entity)
                                            nav.popBackStack()
                                        }
                                    },
                                    onBack = { nav.popBackStack() }
                                )
                            }
                        }
                        composable("fullscreen/{id}") { backStack ->
                            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            val card = cards.find { it.id == id }
                            if (card != null) {
                                FullscreenBarcodeScreen(card = card, onClose = { nav.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
