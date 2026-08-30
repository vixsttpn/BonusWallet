
package com.bonuswallet.app.ui.screens

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.domain.theme.CardThemeResolver
import com.bonuswallet.app.util.BarcodeUtil
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cards: List<CardEntity>,
    onCardClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onReorder: (List<CardEntity>) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }
    var selectedProfile by remember { mutableStateOf("Все") }
    var showOnlyFav by remember { mutableStateOf(false) }
    var voiceQuery by remember { mutableStateOf("") }

    val context = LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            search = matches[0]
        }
    }

    val categories = remember(cards) { listOf("Все") + cards.map { it.category }.distinct().sorted() }
    val profiles = remember(cards) { listOf("Все") + cards.map { it.profileId }.distinct().sorted() }

    val filtered = remember(cards, search, selectedCategory, selectedProfile, showOnlyFav) {
        cards.filter { card ->
            val matchesSearch = if (search.isBlank()) true else {
                val q = search.lowercase()
                card.getDisplayOrgName().lowercase().contains(q) ||
                card.getDisplayTitle().lowercase().contains(q) ||
                card.getDisplayNumber().contains(q) ||
                card.getDisplayNumber().takeLast(4).contains(q) || // 21. Поиск по последним 4 цифрам
                card.category.lowercase().contains(q)
            }
            val matchesCategory = selectedCategory == "Все" || card.category == selectedCategory
            val matchesProfile = selectedProfile == "Все" || card.profileId == selectedProfile
            val matchesFav = !showOnlyFav || card.isFavorite
            matchesSearch && matchesCategory && matchesProfile && matchesFav
        }.sortedWith(compareByDescending<CardEntity> { it.isFavorite }.thenByDescending { it.lastShownAt ?: 0 }.thenBy { it.sortOrder })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BonusWallet") },
                actions = {
                    IconButton(onClick = { showOnlyFav = !showOnlyFav }) {
                        Icon(if(showOnlyFav) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "Избранное")
                    }
                    IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, contentDescription = "Настройки") }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) { Icon(Icons.Default.Add, contentDescription = "Добавить") } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Search + voice 18,21
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Поиск по названию или последним 4 цифрам") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                            }
                            voiceLauncher.launch(intent)
                        }) { Icon(Icons.Default.Mic, contentDescription = "Голосом") }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 2. Категории
            LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // 15. Профили
            LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(profiles) { prof ->
                    FilterChip(
                        selected = selectedProfile == prof,
                        onClick = { selectedProfile = prof },
                        label = { Text(if(prof=="mine") "Мои" else prof) },
                        leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if(cards.isEmpty()) "Нет карт. Добавь первую!" else "Ничего не найдено", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.id }) { card ->
                        RealCardItem(card = card, onClick = { onCardClick(card.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RealCardItem(card: CardEntity, onClick: () -> Unit) {
    val theme = CardThemeResolver.resolve(card.cardTheme)
    val barcode = remember(card) {
        try { BarcodeUtil.generateBarcodeBitmap(card.getDisplayNumber(), card.getDisplayFormat(), 300, 80) } catch(e: Exception){ null }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.background),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            // 12. Фото карты как обложка если есть
            if (card.photoUri != null) {
                AsyncImage(model = card.photoUri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop, alpha = 0.25f)
            }
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(theme.icon, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(card.getDisplayOrgName(), color = theme.textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (card.isFavorite) { Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp)) }
                            }
                            Text(card.getDisplayTitle(), color = theme.secondaryTextColor, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(card.category, color = theme.secondaryTextColor, fontSize = 10.sp, modifier = Modifier.background(theme.textColor.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(2.dp))
                                if(card.showCount>0) Text("${card.showCount}x", color = theme.secondaryTextColor, fontSize = 10.sp)
                            }
                        }
                    }
                    Text(maskNumber(card.getDisplayNumber()), color = theme.secondaryTextColor, fontSize = 11.sp)
                }
                Spacer(Modifier.height(10.dp))
                if (barcode != null) {
                    Image(bitmap = barcode.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).padding(4.dp))
                } else {
                    Text(card.getDisplayNumber(), color = theme.textColor, letterSpacing = 1.sp)
                }
            }
        }
    }
}

fun maskNumber(number: String): String {
    if (number.length <= 8) return number
    return number.take(4) + " •••• " + number.takeLast(4)
}

