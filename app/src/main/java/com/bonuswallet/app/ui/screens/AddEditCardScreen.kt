
package com.bonuswallet.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.domain.detector.CardConfidence
import com.bonuswallet.app.domain.provider.ProviderRegistry
import com.bonuswallet.app.domain.theme.CardThemeResolver
import com.bonuswallet.app.domain.validator.CardInputValidator
import com.bonuswallet.app.domain.validator.ValidationStatus
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    existing: CardEntity?,
    onSave: (CardEntity) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(if (existing != null) 2 else 1) }
    var orgName by remember { mutableStateOf(existing?.getDisplayOrgName() ?: "") }
    var title by remember { mutableStateOf(existing?.getDisplayTitle() ?: "") }
    var number by remember { mutableStateOf(existing?.getDisplayNumber() ?: "") }
    var format by remember { mutableStateOf(existing?.getDisplayFormat() ?: "Автоматически") }
    var providerId by remember { mutableStateOf(existing?.providerId ?: "generic") }
    var searchProviderQuery by remember { mutableStateOf("") }
    var showManualSelection by remember { mutableStateOf(false) }

    var validationResult by remember { mutableStateOf(CardInputValidator.validate(number)) }
    var showBankCardDialog by remember { mutableStateOf(false) }
    var showCvvDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(number) {
        validationResult = CardInputValidator.validate(number)
        if (validationResult.isPaymentCardHighRisk) {
            showBankCardDialog = true
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            number = result.contents
            format = validationResult.barcodeResult.formatString
            step = 2
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scanLauncher.launch(ScanOptions().setOrientationLocked(false).setPrompt("Наведите камеру на штрих-код"))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Добавить карту" else "Изменить карту") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (step == 1 && existing == null) {
                Text("Выберите карту", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Список популярных вариантов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                val popular = ProviderRegistry.getPopularProviders()
                popular.forEach { provider ->
                    val theme = CardThemeResolver.resolve(provider.providerInfo.id)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            providerId = provider.providerInfo.id
                            orgName = provider.providerInfo.displayName
                            step = 2
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(theme.icon, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(provider.providerInfo.displayName, fontWeight = FontWeight.SemiBold)
                                Text(provider.providerInfo.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                OutlinedButton(onClick = { showManualSelection = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Другая карта / Поиск")
                }

                if (showManualSelection) {
                    AlertDialog(
                        onDismissRequest = { showManualSelection = false },
                        title = { Text("Выберите программу") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = searchProviderQuery,
                                    onValueChange = { searchProviderQuery = it },
                                    placeholder = { Text("Поиск: Bravo, Wolt, KFC...") },
                                    leadingIcon = { Icon(Icons.Default.Search, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(Modifier.height(8.dp))
                                val providers = ProviderRegistry.searchProviders(searchProviderQuery)
                                LazyColumn(modifier = Modifier.height(200.dp)) {
                                    items(providers) { p ->
                                        ListItem(
                                            headlineContent = { Text(p.providerInfo.displayName) },
                                            supportingContent = { Text(p.providerInfo.description, style = MaterialTheme.typography.bodySmall) },
                                            modifier = Modifier.clickable {
                                                providerId = p.providerInfo.id
                                                orgName = p.providerInfo.displayName
                                                showManualSelection = false
                                                step = 2
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showManualSelection = false }) { Text("Закрыть") }
                        }
                    )
                }
            }

            if (step >= 2) {
                Text("Шаг 2: Введите номер или сканируйте", fontWeight = FontWeight.Medium)

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Номер карты / штрих-код") },
                    placeholder = { Text("1234567890123") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (perm == PackageManager.PERMISSION_GRANTED) {
                                scanLauncher.launch(ScanOptions().setOrientationLocked(false).setPrompt("Наведите камеру"))
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) { Icon(Icons.Default.QrCodeScanner, contentDescription = "Сканировать") }
                    },
                    isError = validationResult.status == ValidationStatus.PAYMENT_CARD_DETECTED
                )

                if (number.isNotBlank()) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Определяем карту...", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(validationResult.confidenceMessage, style = MaterialTheme.typography.bodySmall)
                            if (validationResult.barcodeResult.isValid) {
                                Text("Формат: ${validationResult.barcodeResult.formatString}", style = MaterialTheme.typography.bodySmall)
                            }
                            validationResult.loyaltyResult.bestMatch?.let {
                                Text("Уровень: ${it.confidence}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (validationResult.paymentDetection.isPotentialPaymentCard) {
                                Text("Подозрение на банковскую карту: ${validationResult.paymentDetection.reason}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (validationResult.loyaltyResult.allMatches.isNotEmpty()) {
                        Text("Возможные варианты:", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                        validationResult.loyaltyResult.allMatches.forEach { match ->
                            val theme = CardThemeResolver.resolve(match.providerId)
                            ListItem(
                                headlineContent = { Text("${theme.icon} ${match.providerId} - ${match.confidence}") },
                                supportingContent = { Text(match.reason) },
                                modifier = Modifier.clickable {
                                    providerId = match.providerId
                                    orgName = ProviderRegistry.getProviderById(match.providerId).providerInfo.displayName
                                }
                            )
                        }
                    }
                }

                if (validationResult.errorMessage != null && !validationResult.isPaymentCardHighRisk) {
                    Text(validationResult.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it },
                    label = { Text("Название организации") },
                    placeholder = { Text("Bravo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название карты (необязательно)") },
                    placeholder = { Text("Моя карта") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextButton(onClick = { showManualSelection = true }) {
                    Text("Выбрать вручную: Я знаю какая это карта")
                }

                if (number.isNotBlank() && orgName.isNotBlank()) {
                    Text("Предпросмотр:", fontWeight = FontWeight.Bold)
                    val previewEntity = CardEntity(
                        organizationName = orgName,
                        orgName = orgName,
                        cardName = title.ifBlank { orgName },
                        title = title.ifBlank { orgName },
                        cardNumber = number,
                        barcodeValue = number,
                        number = number,
                        providerId = providerId,
                        cardTheme = CardThemeResolver.resolve(providerId).id
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardThemeResolver.resolve(providerId).background)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(previewEntity.getDisplayOrgName(), color = CardThemeResolver.resolve(providerId).textColor, fontWeight = FontWeight.Bold)
                            Text(previewEntity.getDisplayTitle(), color = CardThemeResolver.resolve(providerId).secondaryTextColor, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Text(previewEntity.getDisplayNumber(), color = CardThemeResolver.resolve(providerId).textColor)
                        }
                    }
                }

                OutlinedTextField(
                    value = "",
                    onValueChange = { if (it.isNotBlank()) showCvvDialog = true },
                    label = { Text("CVV/CVC (не поддерживается)") },
                    placeholder = { Text("Не вводите CVV") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                Button(
                    onClick = {
                        if (validationResult.isPaymentCardHighRisk) {
                            showBankCardDialog = true
                            return@Button
                        }
                        if (orgName.isBlank()) return@Button
                        val entity = CardEntity(
                            organizationName = orgName.trim(),
                            orgName = orgName.trim(),
                            cardName = title.trim().ifBlank { orgName.trim() },
                            title = title.trim().ifBlank { orgName.trim() },
                            cardNumber = number.trim(),
                            barcodeValue = number.trim(),
                            number = number.trim(),
                            barcodeType = validationResult.barcodeResult.formatString,
                            format = validationResult.barcodeResult.formatString,
                            providerId = providerId,
                            cardTheme = CardThemeResolver.resolve(providerId).id,
                            colorHex = "#111111",
                            sortOrder = 0,
                            status = when (validationResult.loyaltyResult.bestMatch?.confidence) {
                                CardConfidence.HIGH -> "VERIFIED"
                                CardConfidence.MEDIUM -> "FORMAT_VALID"
                                else -> "NOT_CHECKABLE"
                            }
                        )
                        onSave(entity)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = validationResult.canSave && orgName.isNotBlank() && number.isNotBlank()
                ) {
                    Text("Добавить карту")
                }
            }

            if (showBankCardDialog) {
                AlertDialog(
                    onDismissRequest = { showBankCardDialog = false; number = "" },
                    title = { Text("Банковские карты не поддерживаются") },
                    text = {
                        Text("BonusWallet предназначен для скидочных и бонусных карт. Не вводите номера банковских карт, CVV/CVC, PIN или другие платежные данные.\n\nОбнаружено: ${validationResult.paymentDetection.network?.name ?: "банковская карта"}")
                    },
                    confirmButton = {
                        TextButton(onClick = { showBankCardDialog = false; number = "" }) { Text("Понятно") }
                    }
                )
            }

            if (showCvvDialog) {
                AlertDialog(
                    onDismissRequest = { showCvvDialog = false },
                    title = { Text("Платежные реквизиты не поддерживаются") },
                    text = { Text("BonusWallet не предназначен для хранения CVV/CVC, PIN или других платежных данных.") },
                    confirmButton = { TextButton(onClick = { showCvvDialog = false }) { Text("Понятно") } }
                )
            }
        }
    }
}

