
package com.bonuswallet.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.bonuswallet.app.data.AppDatabase
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.domain.detector.CardConfidence
import com.bonuswallet.app.domain.provider.ProviderRegistry
import com.bonuswallet.app.domain.theme.CardThemeResolver
import com.bonuswallet.app.domain.validator.CardInputValidator
import com.bonuswallet.app.domain.validator.ValidationStatus
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    existing: CardEntity?,
    onSave: (CardEntity) -> Unit,
    onBack: () -> Unit
) {
    var orgName by remember { mutableStateOf(existing?.getDisplayOrgName() ?: "") }
    var title by remember { mutableStateOf(existing?.getDisplayTitle() ?: "") }
    var number by remember { mutableStateOf(existing?.getDisplayNumber() ?: "") }
    var providerId by remember { mutableStateOf(existing?.providerId ?: "generic") }
    var category by remember { mutableStateOf(existing?.category ?: "Супермаркеты") }
    var profileId by remember { mutableStateOf(existing?.profileId ?: "mine") }
    var photoUri by remember { mutableStateOf(existing?.photoUri) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var isFavorite by remember { mutableStateOf(existing?.isFavorite ?: false) }

    var searchProviderQuery by remember { mutableStateOf("") }
    var showManualSelection by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf(CardInputValidator.validate(number)) }
    var showBankCardDialog by remember { mutableStateOf(false) }
    var duplicateFound by remember { mutableStateOf<CardEntity?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    LaunchedEffect(number) {
        validationResult = CardInputValidator.validate(number)
        if (validationResult.isPaymentCardHighRisk) showBankCardDialog = true
        if (number.length > 6) {
            val dup = db.cardDao().findDuplicate(number.trim())
            if (dup != null && dup.id != (existing?.id ?: -1)) duplicateFound = dup
            else duplicateFound = null
        }
    }

    // Camera for photo 12
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) photoUri = tempPhotoUri.toString()
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri.toString()
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) number = result.contents
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scanLauncher.launch(ScanOptions().setOrientationLocked(false).setPrompt("Наведите камеру"))
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (existing == null) "Добавить карту" else "Изменить карту") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            if (duplicateFound != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Дубликат найден!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("${duplicateFound!!.getDisplayOrgName()} - ${duplicateFound!!.getDisplayNumber().takeLast(4)}", style = MaterialTheme.typography.bodySmall)
                        Text("Такая карта уже есть в кошельке", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Номер карты / штрих-код") }, modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = {
                    val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (perm == PackageManager.PERMISSION_GRANTED) scanLauncher.launch(ScanOptions().setOrientationLocked(false))
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Icon(Icons.Default.QrCodeScanner, null) } },
                isError = validationResult.status == ValidationStatus.PAYMENT_CARD_DETECTED
            )

            // 12. Фото карты
            Text("Дизайн карты (фото)", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val file = File(context.cacheDir, "card_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                }) { Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(4.dp)); Text("Камера") }
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) { Text("Галерея") }
                if (photoUri != null) TextButton(onClick = { photoUri = null }) { Text("Удалить") }
            }
            if (photoUri != null) {
                AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp))
                Text("Фото будет обрезано как обложка карты", style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("Организация (Bravo, Wolt)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название (необязательно)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            // 2. Категории
            Text("Категория", fontWeight = FontWeight.Medium)
            val cats = listOf("Супермаркеты","Еда","АЗС","Аптеки","Одежда","Красота","Электроника","Другое")
            Row(Modifier.fillMaxWidth().padding(2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // simple dropdown
            }
            var expandedCat by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = !expandedCat }) {
                OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Категория") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                    cats.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { category = c; expandedCat = false }) }
                }
            }

            // 15. Профили
            var expandedProf by remember { mutableStateOf(false) }
            val profs = listOf("mine" to "Мои", "family" to "Семья", "wife" to "Жена", "work" to "Работа")
            ExposedDropdownMenuBox(expanded = expandedProf, onExpandedChange = { expandedProf = !expandedProf }) {
                OutlinedTextField(value = profs.find { it.first == profileId }?.second ?: profileId, onValueChange = {}, readOnly = true, label = { Text("Профиль") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProf) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expandedProf, onDismissRequest = { expandedProf = false }) {
                    profs.forEach { p -> DropdownMenuItem(text = { Text(p.second) }, onClick = { profileId = p.first; expandedProf = false }) }
                }
            }

            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Заметки (необязательно)") }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                Text("В избранное (для виджета)")
            }

            TextButton(onClick = { showManualSelection = true }) { Text("Выбрать бренд вручную") }

            if (showManualSelection) {
                AlertDialog(onDismissRequest = { showManualSelection = false }, title = { Text("Выберите") }, text = {
                    Column {
                        OutlinedTextField(value = searchProviderQuery, onValueChange = { searchProviderQuery = it }, placeholder = { Text("Bravo, Wolt...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth())
                        LazyColumn(Modifier.height(200.dp)) {
                            items(ProviderRegistry.searchProviders(searchProviderQuery)) { p ->
                                ListItem(headlineContent = { Text(p.providerInfo.displayName) }, modifier = Modifier.clickable { providerId = p.providerInfo.id; orgName = p.providerInfo.displayName; showManualSelection = false })
                            }
                        }
                    }
                }, confirmButton = { TextButton(onClick = { showManualSelection = false }) { Text("Закрыть") } })
            }

            Button(
                onClick = {
                    if (validationResult.isPaymentCardHighRisk) { showBankCardDialog = true; return@Button }
                    if (orgName.isBlank() || number.isBlank()) return@Button
                    val entity = (existing ?: CardEntity()).copy(
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
                        category = category,
                        profileId = profileId,
                        photoUri = photoUri,
                        notes = notes.ifBlank { null },
                        isFavorite = isFavorite,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(entity)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = validationResult.canSave && orgName.isNotBlank() && number.isNotBlank() && duplicateFound == null,
                shape = RoundedCornerShape(14.dp)
            ) { Text(if(existing==null) "Добавить карту" else "Сохранить") }

            if (showBankCardDialog) {
                AlertDialog(onDismissRequest = { showBankCardDialog = false; number = "" }, title = { Text("Банковские карты нельзя") }, text = { Text("Не вводите банковские карты") }, confirmButton = { TextButton(onClick = { showBankCardDialog = false; number = "" }) { Text("Ок") } })
            }
        }
    }
}

