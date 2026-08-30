
package com.bonuswallet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.util.BarcodeUtil

@Composable
fun HomeScreen(
    cards: List<CardEntity>,
    onCardClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReorder: (List<CardEntity>) -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("BonusWallet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("Мои карты", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, contentDescription = "Настройки") }
        }

        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("У вас пока нет карт", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Добавьте первую карту, чтобы быстро показывать её на кассе.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 20.sp)
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onAddClick, modifier = Modifier.height(50.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить карту")
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)) {
                items(cards, key = { it.id }) { card ->
                    CardItem(card = card, onClick = { onCardClick(card.id) })
                }
            }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        if (cards.isNotEmpty()) {
            Button(onClick = onAddClick, modifier = Modifier.padding(bottom = 24.dp).height(56.dp).fillMaxWidth(0.9f), shape = RoundedCornerShape(28.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить карту")
            }
        }
    }
}

@Composable
fun CardItem(card: CardEntity, onClick: () -> Unit) {
    val bg = try { Color(android.graphics.Color.parseColor(card.colorHex)) } catch(e: Exception){ Color(0xFF171717) }
    val isDark = bg.red+bg.green+bg.blue < 1.5f
    val textColor = if (isDark) Color.White else Color(0xFF171717)

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).clickable(onClick=onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(bg), contentAlignment = Alignment.Center) {
                        Text(card.orgName.take(1).uppercase(), color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(card.orgName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(card.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(card.format, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(maskNumber(card.number), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Text("Открыть →", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

fun maskNumber(num: String): String {
    if (num.length <= 4) return num
    return "••• ${num.takeLast(4)}"
}
