package com.bonuswallet.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.ui.components.BonusDashboard
import com.bonuswallet.app.ui.components.PremiumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cards: List<CardEntity>,
    onCardClick: (CardEntity) -> Unit,
    onAdd: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val totalBonuses = cards.sumOf { it.bonusBalance }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) { Icon(Icons.Default.Add, null) }
        }
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Храните карты", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), lineHeight = 34.sp)
                Text("все бонусные карты в одном приложении", fontSize = 15.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(16.dp))
            }

            if (cards.isNotEmpty()) {
                item {
                    // Веер карт как на 1 скрине
                    Box(
                        Modifier.fillMaxWidth().height(if (expanded) (cards.size * 68 + 60).dp else 180.dp)
                           .animateContentSize(spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.8f))
                           .clip(RoundedCornerShape(24.dp))
                           .background(Color.White)
                           .padding(16.dp)
                    ) {
                        cards.take(6).forEachIndexed { i, card ->
                            PremiumCard(card = card, index = i, expanded = expanded, onClick = { if (cards.size > 2) expanded =!expanded else onCardClick(card) })
                        }
                        if (cards.size > 4 &&!expanded) {
                            Box(Modifier.align(Alignment.BottomCenter).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F172A)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                                Text("+${cards.size - 3} еще • нажать чтобы раскрыть", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    BonusDashboard(total = if (totalBonuses == 0) 1240 else totalBonuses, cardsCount = cards.size)
                }

                item {
                    // Достижения
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        AchievementBadge("🏆", "1240", "баллов", Modifier.weight(1f))
                        AchievementBadge("📚", "${cards.size}", "карт", Modifier.weight(1f))
                        AchievementBadge("⭐", "Gold", "уровень", Modifier.weight(1f))
                    }
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF16A34A), Color(0xFF22C55E)))).padding(24.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Добавьте первую карту", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("Bravo, Umico, KFC - авто цвета", color = Color.White.copy(0.9f), fontSize = 13.sp)
                        }
                    }
                }
            }

            itemsIndexed(cards) { idx, card ->
                PremiumCard(card = card, index = 0, expanded = true, onClick = { onCardClick(card) })
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AchievementBadge(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).padding(14.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CardItem(card: CardEntity, onClick: () -> Unit) {
    PremiumCard(card = card, index = 0, expanded = true, onClick = onClick)
}
