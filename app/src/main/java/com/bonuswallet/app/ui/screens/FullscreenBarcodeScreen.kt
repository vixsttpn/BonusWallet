
package com.bonuswallet.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.CardEntity
import com.bonuswallet.app.data.CardShowHistory
import com.bonuswallet.app.data.AppDatabase
import com.bonuswallet.app.util.BarcodeUtil
import kotlinx.coroutines.launch
import android.provider.Settings

@Composable
fun FullscreenBarcodeScreen(card: CardEntity, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inverted by remember { mutableStateOf(false) }
    val db = remember { AppDatabase.getInstance(context) }

    // 3. Автояркость на 100% и 22. Защита от скрина
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val originalBrightness = window?.attributes?.screenBrightness ?: -1f
        val originalFlags = window?.attributes?.flags ?: 0

        window?.let {
            val layoutParams = it.attributes
            layoutParams.screenBrightness = 1.0f // 3. Автояркость 100%
            it.attributes = layoutParams
            it.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // 22. Защита от скрина
            it.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // 20. История показов
        scope.launch {
            db.cardDao().markShown(card.id, System.currentTimeMillis())
            db.cardDao().insertHistory(CardShowHistory(cardId = card.id))
        }

        onDispose {
            window?.let {
                val layoutParams = it.attributes
                layoutParams.screenBrightness = originalBrightness
                it.attributes = layoutParams
                it.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                it.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val bitmap: Bitmap? = remember(card, inverted) {
        try {
            val bmp = BarcodeUtil.generateBarcodeBitmap(card.getDisplayNumber(), card.getDisplayFormat(), 900, 320)
            if (inverted && bmp != null) {
                // 11. Инвертированный для кассы - делаем негатив
                val invertedBmp = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)
                for (x in 0 until bmp.width) {
                    for (y in 0 until bmp.height) {
                        val pixel = bmp.getPixel(x, y)
                        val r = 255 - android.graphics.Color.red(pixel)
                        val g = 255 - android.graphics.Color.green(pixel)
                        val b = 255 - android.graphics.Color.blue(pixel)
                        invertedBmp.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
                    }
                }
                invertedBmp
            } else bmp
        } catch(e: Exception) { null }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(if(inverted) Color.Black else Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = if(inverted) Color.White else Color.Black) }
                Row {
                    // 11. Инверт
                    IconButton(onClick = { inverted = !inverted }) { Icon(Icons.Default.InvertColors, contentDescription = "Инверт", tint = if(inverted) Color.White else Color.Black) }
                    // 10. Поделиться с семьей
                    IconButton(onClick = {
                        scope.launch {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Карта ${card.getDisplayOrgName()}: ${card.getDisplayNumber()} (${card.getDisplayFormat()})")
                            }
                            context.startActivity(Intent.createChooser(intent, "Поделиться картой"))
                        }
                    }) { Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = if(inverted) Color.White else Color.Black) }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(card.getDisplayOrgName(), color = if(inverted) Color.White else Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(card.getDisplayTitle(), color = if(inverted) Color.Gray else Color.DarkGray, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(24.dp))

            if (bitmap != null) {
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Штрих-код", modifier = Modifier.fillMaxWidth().background(if(inverted) Color.White else Color.Transparent, RoundedCornerShape(12.dp)).padding(12.dp))
            } else {
                Text("Не удалось сгенерировать штрих-код", color = if(inverted) Color.White else Color.Black)
            }

            Spacer(Modifier.height(16.dp))
            Text(card.getDisplayNumber(), color = if(inverted) Color.White else Color.Black, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally), letterSpacing = 2.sp)

            Spacer(Modifier.weight(1f))
            Text(if(inverted) "Инвертированный режим для сканера" else "Яркость 100% включена", color = if(inverted) Color.Gray else Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
        }
    }
}

