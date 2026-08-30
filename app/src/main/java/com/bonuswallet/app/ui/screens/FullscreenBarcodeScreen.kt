
package com.bonuswallet.app.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.AppDatabase
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.util.BarcodeUtil
import kotlinx.coroutines.launch

@Composable
fun FullscreenBarcodeScreen(card: CardEntity, onClose: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    LaunchedEffect(card) {
        bitmap = BarcodeUtil.generateBitmap(card.getDisplayNumber(), card.getDisplayFormat(), 1600, if (card.getDisplayFormat() == "QR Code") 1600 else 600)
        // Update lastUsedAt - requirement 21
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                db.cardDao().updateLastUsed(card.id)
            } catch (e: Exception) { }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White).clickable { onClose() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(card.getDisplayOrgName(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black); Text(card.getDisplayTitle(), color = Color.Gray) }
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.Black) }
            }
            Spacer(Modifier.weight(1f))
            if (bitmap != null) {
                Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = card.getDisplayNumber(), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Text(card.getDisplayNumber(), color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("Покажите карту на кассе • Сканирование штрих-кода", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

