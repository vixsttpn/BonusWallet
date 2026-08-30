package com.bonuswallet.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bonuswallet.app.data.BrandRegistry
import com.bonuswallet.app.data.CardEntity

@Composable
fun PremiumCard(
    card: CardEntity,
    index: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brand = BrandRegistry.resolve(card.orgName)
    val rotation = if (expanded) index * 2f - 3f else index * -4.5f + 10f
    val offset = if (expanded) index * 16 else index * 24

    val infinite = rememberInfiniteTransition(label = "shine")
    val shineX by infinite.animateFloat(
        initialValue = -100f, targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "shine"
    )

    Box(
        modifier = modifier
           .graphicsLayer {
                rotationZ = rotation
                translationY = offset.toFloat()
            }
           .shadow(18.dp, RoundedCornerShape(18.dp), spotColor = brand.primaryColor.copy(0.35f))
           .clip(RoundedCornerShape(18.dp))
           .background(Brush.linearGradient(listOf(brand.primaryColor, brand.secondaryColor)))
           .clickable { onClick() }
           .fillMaxWidth()
           .height(104.dp)
           .padding(16.dp)
    ) {
        Row(Modifier.fillMaxSize(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.22f)), Alignment.Center) {
                    Text(brand.logoEmoji, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text(brand.displayName, color = brand.textColor, fontWeight = FontWeight.Black, fontSize = 17.sp, lineHeight = 18.sp)
                    Spacer(Modifier.height(2.dp))
                    if (card.bonusBalance > 0) {
                        Text("+${brand.bonusPercent}% • ${card.bonusBalance} баллов", color = brand.textColor.copy(0.9f), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Loyalty Card", color = brand.textColor.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(Modifier.width(56.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).background(Color.White), Alignment.Center) {
                    Text("⁞⁞⁞⁞", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text("ID: ${card.number.takeLast(4)}", color = brand.textColor.copy(0.6f), fontSize = 9.sp)
            }
        }
        // Блеск 60 FPS
        Box(
            Modifier.fillMaxSize().graphicsLayer { translationX = shineX }
               .background(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(0.18f), Color.Transparent)))
        )
    }
}

@Composable
fun BonusDashboard(total: Int, cardsCount: Int) {
    val progress = (total % 1500) / 1500f
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Ваши бонусы", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$total", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text("баллов", fontSize = 15.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 7.dp))
            }
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("До следующего уровня", fontSize = 12.sp, color = Color.Gray)
                    Text("• 260 до Gold", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE5E7EB))) {
                    Box(Modifier.fillMaxWidth(progress.coerceAtLeast(0.15f)).height(10.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF334155)))))
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                ActionChip("Кешбэк 5%", "%", Color(0xFF22C55E))
                ActionChip("Купоны", "🎟️", Color(0xFFF97316))
                ActionChip("История", "🕒", Color(0xFF60A5FA))
                ActionChip("Акции", "🎁", Color(0xFFA78BFA))
            }
            if (cardsCount > 0) Text("$cardsCount карт • Мобильный кошелёк • удобно и выгодно", fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun ActionChip(title: String, icon: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(0.16f)), Alignment.Center) { Text(icon, fontSize = 22.sp) }
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
    }
}
