
package com.bonuswallet.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.util.BackupUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    cards: List<CardEntity>,
    onImport: (List<CardEntity>) -> Unit,
    onDeleteAll: () -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteAll by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try {
                val json = BackupUtil.export(cards)
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                message = "Экспорт выполнен"
            } catch (e: Exception) { message = "Ошибка экспорта: ${e.message}" }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: throw Exception("Пустой файл")
                val imported = BackupUtil.import(text)
                onImport(imported)
                message = "Импортировано ${imported.size} карт"
            } catch (e: Exception) {
                message = "Ошибка импорта: ${e.message}"
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Настройки") }, navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

            Text("Внешний вид", fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "Системная", "light" to "Светлая", "dark" to "Темная").forEach { (key, label) ->
                    Row(Modifier.fillMaxWidth()) {
                        RadioButton(selected = currentTheme == key, onClick = { onThemeChange(key) })
                        TextButton(onClick = { onThemeChange(key) }){ Text(label) }
                    }
                }
            }

            Divider()

            Text("Карты", fontWeight = FontWeight.SemiBold)
            Button(onClick = { exportLauncher.launch("BonusWallet_backup.json") }, modifier = Modifier.fillMaxWidth()) { Text("Экспорт карт") }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) { Text("Импорт карт") }
            OutlinedButton(onClick = { showDeleteAll = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Удалить все карты") }

            Divider()

            Text("Конфиденциальность", fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = onPrivacy, modifier = Modifier.fillMaxWidth()) { Text("Политика конфиденциальности") }
            OutlinedButton(onClick = onTerms, modifier = Modifier.fillMaxWidth()) { Text("Правила использования") }

            Divider()

            Text("О приложении", fontWeight = FontWeight.SemiBold)
            Text("BonusWallet\nВерсия 1.0.0\n© 2026 BonusWallet. Все права защищены.")
            Text("BonusWallet является независимым приложением и не является официальным приложением Bravo, Wolt, KFC и других компаний.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

            if (message != null) {
                Card { Text(message!!, modifier = Modifier.padding(12.dp)) }
            }
        }
    }

    if (showDeleteAll) {
        AlertDialog(
            onDismissRequest = { showDeleteAll = false },
            title = { Text("Удалить все карты?") },
            text = { Text("Все карты будут удалены с устройства. Это действие нельзя отменить.") },
            confirmButton = { TextButton(onClick = { showDeleteAll = false; onDeleteAll() }){ Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDeleteAll = false }){ Text("Отмена") } }
        )
    }
}
