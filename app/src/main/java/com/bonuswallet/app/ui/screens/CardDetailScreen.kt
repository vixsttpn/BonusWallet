
package com.bonuswallet.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.util.BarcodeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    card: CardEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFullscreen: () -> Unit,
    onBack: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(card) {
        bitmap = BarcodeUtil.generateBitmap(card.number, card.format, 1200, 500)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card.title) },
                navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } },
                actions = {
                    IconButton(onClick = onEdit){ Icon(Icons.Default.Edit, contentDescription = "Изменить") }
                    IconButton(onClick = { showDeleteConfirm = true }){ Icon(Icons.Default.Delete, contentDescription = "Удалить") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp)) {
                    Text(card.orgName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(card.title, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(6.dp))
                    Text(card.format, style = MaterialTheme.typography.labelSmall)
                }
            }

            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if (bitmap != null) {
                        Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = "Штрих-код ${card.number}", modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(androidx.compose.ui.graphics.Color.White).padding(12.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(card.number, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                    } else {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Генерация штрих-кода...")
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onFullscreen, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("На весь экран")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить карту?") },
            text = { Text("Карта будет удалена с этого устройства. Это действие нельзя отменить.") },
            confirmButton = { TextButton(onClick = { showDeleteConfirm = false; onDelete() }){ Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }){ Text("Отмена") } }
        )
    }
}
