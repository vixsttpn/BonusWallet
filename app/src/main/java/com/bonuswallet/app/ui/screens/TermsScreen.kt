
package com.bonuswallet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TermsScreen(onAccepted: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Добро пожаловать в BonusWallet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Условия использования", fontWeight = FontWeight.Bold)
        Text("1. BonusWallet предназначен только для скидочных, бонусных, клубных карт.")
        Text("2. ЗАПРЕЩЕНО добавлять банковские карты, CVV/CVC, PIN, платежные реквизиты.")
        Text("3. Приложение пытается определить тип карты, но не гарантирует 100% точность. Высокая достоверность только при наличии официального формата.")
        Text("4. Баланс показывается только если получен из реального источника. Мы не выдумываем балансы.")
        Text("5. Все данные хранятся локально. Мы не отправляем номера карт на неизвестные серверы.")
        Text("6. Если официальный API недоступен, приложение честно показывает \"Проверка недоступна\".")
        Text("7. Разрешено использовать приложение как пользователю. Запрещено копировать, декомпилировать, публиковать в сторах.")
        Text("8. Проект не представлен в соцсетях. Единственный источник - GitHub репозиторий.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAccepted, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Принимаю условия")
        }
    }
}

