package com.bonuswallet.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.util.BarcodeUtil
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    existing: CardEntity?,
    onSave: (CardEntity) -> Unit,
    onBack: () -> Unit
) {
    var orgName by remember { mutableStateOf(existing?.orgName ?: "") }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var number by remember { mutableStateOf(existing?.number ?: "") }
    var format by remember { mutableStateOf(existing?.format ?: "Автоматически") }
    var colorHex by remember { mutableStateOf(existing?.colorHex ?: "#111111") }
    var error by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            number = result.contents
            format = BarcodeUtil.detectFormat(result.contents)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scanLauncher.launch(ScanOptions().setOrientationLocked(false).setPrompt("Наведите камеру на штрих-код"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (existing == null) "Добавить карту" else "Изменить карту") },
                navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("Название организации") }, placeholder = { Text("Bravo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название карты") }, placeholder = { Text("Моя карта Bravo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(
                value = number,
                onValueChange = { number = it; error = null },
                label = { Text("Номер / штрих-код") },
                placeholder = { Text("1234567890123") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        val camPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (camPerm == PackageManager.PERMISSION_GRANTED) {
                            scanLauncher.launch(ScanOptions().setOrientationLocked(false).setPrompt("Наведите камеру на штрих-код"))
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) { Icon(Icons.Default.QrCodeScanner, contentDescription = "Сканировать") }
                }
            )
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = format, onValueChange = {}, readOnly = true, label = { Text("Тип штрих-кода") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    for (fmt in BarcodeUtil.SUPPORTED_FORMATS) {
                        DropdownMenuItem(text = { Text(fmt) }, onClick = { format = fmt; expanded = false })
                    }
                }
            }

            Text("Цвет карты", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (hex in listOf("#111111","#1a9d5e","#ff3b30","#007aff","#ffcc00","#8e44ad","#ff6b00")) {
                    val selected = colorHex == hex
                    FilterChip(selected = selected, onClick = { colorHex = hex }, label = { Text(" ") }, modifier = Modifier.size(40.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val validation = BarcodeUtil.validate(number, format)
                    if (validation != null) { error = validation; return@Button }
                    if (orgName.isBlank()) { error = "Введите название организации"; return@Button }
                    if (title.isBlank()) title = orgName
                    val entity = (existing?.copy(
                        orgName = orgName.trim(),
                        title = title.trim(),
                        number = number.trim(),
                        format = format,
                        colorHex = colorHex
                    ) ?: CardEntity(
                        orgName = orgName.trim(),
                        title = title.trim().ifBlank { orgName.trim() },
                        number = number.trim(),
                        format = format,
                        colorHex = colorHex,
                        sortOrder = 0
                    ))
                    onSave(entity)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Сохранить карту", fontWeight = FontWeight.SemiBold) }

            Text("Сканирование использует камеру только для считывания. Ручной ввод всегда доступен.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
