
package com.bonuswallet.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.domain.provider.BalanceStatus
import com.bonuswallet.app.domain.provider.ProviderRegistry
import com.bonuswallet.app.domain.theme.CardThemeResolver
import com.bonuswallet.app.util.BalanceFormatter
import com.bonuswallet.app.util.BarcodeUtil
import kotlinx.coroutines.launch

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
    var balanceResult by remember { mutableStateOf<com.bonuswallet.app.domain.provider.BalanceResult?>(null) }
    var isCheckingBalance by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val theme = CardThemeResolver.resolve(card.providerId)

    LaunchedEffect(card) {
        bitmap = BarcodeUtil.generateBitmap(card.getDisplayNumber(), card.getDisplayFormat(), 1200, 500)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card.getDisplayTitle()) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Изменить") }
                    IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, contentDescription = "Удалить") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = theme.background)) {
                Column(Modifier.padding(18.dp)) {
                    Text(card.getDisplayOrgName(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = theme.textColor)
                    Text(card.getDisplayTitle(), color = theme.secondaryTextColor)
                    Spacer(Modifier.height(6.dp))
                    Text(card.getDisplayFormat(), style = MaterialTheme.typography.labelSmall, color = theme.secondaryTextColor)
                    Spacer(Modifier.height(6.dp))
                    when (card.status) {
                        "VERIFIED" -> Text("✓ Подтверждено", color = theme.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        "FORMAT_VALID" -> Text("Определено по формату", color = theme.secondaryTextColor, fontSize = 12.sp)
                        else -> Text("Не проверено", color = theme.secondaryTextColor, fontSize = 12.sp)
                    }
                }
            }

            // Barcode
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (bitmap != null) {
                        Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = card.getDisplayNumber(), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        Text(card.getDisplayNumber(), fontWeight = FontWeight.Medium)
                    } else {
                        Text("Не удалось сгенерировать штрих-код")
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onFullscreen) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Открыть на весь экран")
                    }
                }
            }

            // Balance section - honest, no fake
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Баланс", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (!isCheckingBalance) {
                            TextButton(onClick = {
                                scope.launch {
                                    isCheckingBalance = true
                                    errorMessage = null
                                    try {
                                        val provider = ProviderRegistry.getProviderById(card.providerId)
                                        val result = provider.getBalance(card.getDisplayNumber())
                                        balanceResult = result
                                        errorMessage = result.message
                                    } catch (e: Exception) {
                                        errorMessage = "Не удалось получить актуальный баланс: ${e.message}"
                                    } finally {
                                        isCheckingBalance = false
                                    }
                                }
                            }) { Text("Обновить") }
                        }
                    }

                    if (isCheckingBalance) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Проверяем баланс...")
                        }
                    } else {
                        if (card.isBalanceReal()) {
                            Text(
                                BalanceFormatter.formatBalanceForCard(card.balance, card.bonusPoints, card.cashBalance, card.currency, card.balanceAvailable),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            card.lastBalanceUpdate?.let {
                                Text(BalanceFormatter.formatLastUpdated(it), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        } else {
                            // Show last known if offline and had real balance before
                            if (card.balance != null || card.bonusPoints != null) {
                                Text("Последний известный баланс:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    BalanceFormatter.formatBalanceForCard(card.balance, card.bonusPoints, card.cashBalance, card.currency, true),
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Может быть устаревшим", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                card.lastBalanceUpdate?.let {
                                    Text(BalanceFormatter.formatLastUpdated(it), fontSize = 11.sp)
                                }
                            } else {
                                Text("Баланс недоступен", fontWeight = FontWeight.Medium)
                                Text(
                                    "Эта программа не предоставляет баланс через доступный способ проверки. Проверьте баланс в официальном приложении ${card.getDisplayOrgName()}.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        balanceResult?.let {
                            Spacer(Modifier.height(8.dp))
                            when (it.status) {
                                BalanceStatus.UNSUPPORTED -> Text(it.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                BalanceStatus.REQUIRES_AUTH -> Text(it.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                BalanceStatus.NETWORK_ERROR -> Text("Не удалось получить актуальный баланс", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                BalanceStatus.SUCCESS -> {
                                    // Real success - would update DB in parent, but here just show
                                    Text("Обновлено только что", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                else -> Text(it.message, fontSize = 12.sp)
                            }
                        }

                        errorMessage?.let {
                            Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Text("ID карты: ${card.id} • Создана: ${java.text.SimpleDateFormat("dd.MM.yyyy").format(java.util.Date(card.createdAt))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Удалить карту?") },
                text = { Text("Карта ${card.getDisplayOrgName()} будет удалена") },
                confirmButton = { TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("Удалить") } },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") } }
            )
        }
    }
}

