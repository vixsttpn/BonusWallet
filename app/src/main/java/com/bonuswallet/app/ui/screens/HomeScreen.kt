
package com.bonuswallet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.bonuswallet.app.domain.theme.CardThemeResolver
import com.bonuswallet.app.util.BalanceFormatter

enum class SortType {
    LAST_USED, NAME_AZ, MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cards: List<CardEntity>,
    onCardClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReorder: (List<CardEntity>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(SortType.LAST_USED) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredCards = remember(cards, searchQuery, sortType) {
        val filtered = if (searchQuery.isBlank()) cards else cards.filter {
            it.getDisplayOrgName().contains(searchQuery, ignoreCase = true) ||
            it.getDisplayTitle().contains(searchQuery, ignoreCase = true) ||
            it.getDisplayNumber().contains(searchQuery, ignoreCase = true) ||
            it.providerId.contains(searchQuery, ignoreCase = true) ||
            it.getDisplayNumber().takeLast(4).contains(searchQuery)
        }

        when (sortType) {
            SortType.LAST_USED -> filtered.sortedByDescending { it.lastUsedAt ?: it.createdAt }
            SortType.NAME_AZ -> filtered.sortedBy { it.getDisplayOrgName() }
            SortType.MANUAL -> filtered.sortedBy { it.sortOrder }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BonusWallet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Мои карты", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Text("⇅", fontSize = 18.sp)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                placeholder = { Text("Поиск по названию, номеру, последним цифрам") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (showSortMenu) {
                AlertDialog(
                    onDismissRequest = { showSortMenu = false },
                    title = { Text("Сортировка") },
                    text = {
                        Column {
                            TextButton(onClick = { sortType = SortType.LAST_USED; showSortMenu = false }, modifier = Modifier.fillMaxWidth()) { Text("Последние использованные сверху") }
                            TextButton(onClick = { sortType = SortType.NAME_AZ; showSortMenu = false }, modifier = Modifier.fillMaxWidth()) { Text("Название А-Я") }
                            TextButton(onClick = { sortType = SortType.MANUAL; showSortMenu = false }, modifier = Modifier.fillMaxWidth()) { Text("Ручной порядок") }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showSortMenu = false }) { Text("Закрыть") } }
                )
            }

            if (filteredCards.isEmpty() && searchQuery.isBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("У вас пока нет карт", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Добавьте карту Bravo, Wolt, KFC или другую", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onAddClick) { Text("Добавить карту") }
                    }
                }
            } else if (filteredCards.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ничего не найдено по запросу \"$searchQuery\"")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCards, key = { it.id }) { card ->
                        RealCardItem(card = card, onClick = { onCardClick(card.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RealCardItem(card: CardEntity, onClick: () -> Unit) {
    val theme = CardThemeResolver.resolve(card.providerId.ifBlank { CardThemeResolver.resolveByOrgName(card.getDisplayOrgName()).id })

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.background),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(theme.secondaryBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(theme.icon, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(card.getDisplayOrgName(), color = theme.textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(card.getDisplayTitle(), color = theme.secondaryTextColor, fontSize = 13.sp)
                        if (card.providerId != "generic" && card.providerId.isNotBlank()) {
                            Text(card.providerId, color = theme.secondaryTextColor.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
                // Verification badge - honest
                when (card.status) {
                    "VERIFIED" -> BadgeGreen("✓ Подтверждено")
                    "FORMAT_VALID" -> BadgeNeutral("Определено по формату")
                    else -> BadgeNeutral("Не проверено")
                }
            }

            Spacer(Modifier.height(14.dp))

            // Balance - only if real
            if (card.isBalanceReal()) {
                Text(
                    BalanceFormatter.formatBalanceForCard(card.balance, card.bonusPoints, card.cashBalance, card.currency, card.balanceAvailable),
                    color = theme.textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (card.lastBalanceUpdate != null) {
                    Text(BalanceFormatter.formatLastUpdated(card.lastBalanceUpdate), color = theme.secondaryTextColor, fontSize = 11.sp)
                }
            } else {
                // Don't show fake balance - show unavailable honestly
                Text("Баланс недоступен", color = theme.secondaryTextColor, fontSize = 12.sp)
                Text("Проверка баланса недоступна для этой программы", color = theme.secondaryTextColor.copy(0.7f), fontSize = 10.sp)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(card.getDisplayFormat(), color = theme.secondaryTextColor, fontSize = 11.sp)
                    Text(maskNumber(card.getDisplayNumber()), color = theme.textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    if (card.lastUsedAt != null) {
                        Text("Использовали недавно", color = theme.secondaryTextColor, fontSize = 10.sp)
                    }
                }
                Surface(color = theme.secondaryBackground, shape = RoundedCornerShape(8.dp)) {
                    Text("Открыть →", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun BadgeGreen(text: String) {
    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BadgeNeutral(text: String) {
    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(6.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFF6B7280))
    }
}

fun maskNumber(num: String): String {
    if (num.length <= 4) return num
    return "••• ${num.takeLast(4)}"
}

