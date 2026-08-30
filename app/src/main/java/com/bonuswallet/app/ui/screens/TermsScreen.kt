
package com.bonuswallet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsScreen(onAccepted: () -> Unit) {
    var scrolledToEnd by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    
    LaunchedEffect(scroll.value, scroll.maxValue) {
        if (scroll.maxValue > 0 && scroll.value >= scroll.maxValue - 50) {
            scrolledToEnd = true
        }
        if (scroll.maxValue == 0) {
            // short content, allow after small delay
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Правила использования и политика конфиденциальности BonusWallet", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 28.sp))
        Spacer(Modifier.height(12.dp))
        Text("Версия 1.0.0 от 30 августа 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(12.dp))

        Column(Modifier.weight(1f).verticalScroll(scroll).padding(bottom = 12.dp)) {
            PolicyText()
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onAccepted,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = scrolledToEnd || scroll.maxValue == 0
        ) {
            Text("Ознакомился, принимаю")
        }
        if (!scrolledToEnd && scroll.maxValue > 0) {
            Text("Прокрутите документ до конца", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top=8.dp))
        }
    }
}

@Composable
fun PolicyText() {
    val sections = listOf(
        "1. Что такое BonusWallet" to "BonusWallet — это локальный кошелек для хранения ваших скидочных, бонусных и клубных карт. Приложение позволяет хранить номера карт и генерировать штрих-коды для показа на кассе. \n\n© 2026 BonusWallet. Все права защищены.",
        "2. Какие данные обрабатываются" to "Приложение обрабатывает только те данные, которые вы вводите вручную:\n• Название организации (например Bravo, Wolt, KFC)\n• Название карты\n• Номер/значение штрих-кода\n• Выбранный тип штрих-кода\n• Цвет оформления и порядок сортировки\n\nМы НЕ собираем: email, телефон, пароль, имя, адрес, платежные данные.",
        "3. Для чего используются данные" to "Данные используются исключительно для:\n• Отображения списка карт\n• Генерации штрих-кода на экране\n• Локального хранения на устройстве\n• Сортировки и управления картами",
        "4. Где хранятся данные" to "Все данные хранятся локально на вашем устройстве в базе данных Room и DataStore. Данные не отправляются на сервер BonusWallet, так как сервера не существует. Приложение работает полностью офлайн.\n\nПри удалении приложения данные удаляются вместе с ним, если вы не сделали резервную копию.",
        "5. Серверы и передача третьим лицам" to "BonusWallet НЕ имеет серверов. Данные НЕ передаются третьим лицам, не продаются и не анализируются удаленно. Приложение не содержит трекеров, аналитики, рекламы.",
        "6. Аналитика и реклама" to "В версии 1.0.0 аналитика и реклама отсутствуют. Мы не используем Firebase Analytics, Facebook SDK, AdMob и подобные сервисы.",
        "7. Cookies" to "Мобильное приложение не использует cookies. Если в будущем появится сайт bonuswallet.app, он может использовать технические cookies.",
        "8. Разрешения Android" to "Приложение запрашивает:\n• CAMERA — только для сканирования штрих-кода камерой при добавлении карты. Использование камеры является опциональным. Вы можете всегда ввести номер вручную. Фотографии не сохраняются и не отправляются.\n\nРазрешение INTERNET НЕ запрашивается в базовой версии. Приложение работает без интернета.",
        "9. Для чего нужна камера" to "Камера используется только для распознавания штрих-кода с пластиковой карты. После сканирования значение помещается в поле ввода. Камера не используется для идентификации личности.",
        "10. Удаление данных" to "Вы можете:\n• Удалить отдельную карту — кнопка Удалить\n• Удалить все карты — Настройки → Удалить все карты\n• Удалить все данные — удалить приложение\n• Экспорт не создает копию на сервере, файл хранится там, куда вы его сохранили.",
        "11. Что происходит при удалении приложения" to "При удалении приложения Android удаляет базу данных Room, настройки и все карты. Восстановление возможно только из ранее экспортированного вами файла BonusWallet_backup.json.",
        "12. Безопасность локальных данных" to "Данные хранятся в приватной директории приложения, недоступной другим приложениям без root. Не логируем номера карт в Logcat. Не храним данные в открытом виде вне защищенной БД. Рекомендуем блокировать телефон паролем/биометрией.",
        "13. Ограничение ответственности" to "BonusWallet является независимым приложением и не является официальным приложением Bravo, Wolt, KFC и других компаний. Названия и товарные знаки принадлежат соответствующим правообладателям. Использование названий необходимо только для идентификации карты пользователя.\n\nПользователь обязан иметь право использовать добавляемые карты. Запрещается использование для мошенничества, подделки, незаконного доступа, обхода систем лояльности, использования чужих карт без разрешения.\n\nBonusWallet не гарантирует, что сторонняя организация всегда примет отображаемый штрих-код. Компании могут изменить формат карт, систему лояльности и правила.",
        "14. Сторонние сервисы и бренды" to "В приложении используются open-source библиотеки: AndroidX, Jetpack Compose, Room, ZXing. Их лицензии соблюдаются. Логотипы Bravo, Wolt, KFC не используются в приложении во избежание нарушения прав.",
        "15. Изменения политики" to "При изменении политики новая версия будет показана при обновлении приложения. Продолжение использования означает согласие.",
        "16. Контакт" to "По вопросам конфиденциальности: privacy@bonuswallet.app (заглушка, в версии 1.0.0 обращения обрабатываются через страницу приложения).",
        "17. Запрет на копирование" to "Исходный код, дизайн, структура, графические элементы, тексты и другие оригинальные материалы BonusWallet не разрешается копировать, распространять, продавать или использовать в другом продукте без разрешения правообладателя. © 2026 BonusWallet. Все права защищены.",
        "18. Итог" to "BonusWallet создан для удобного и приватного хранения ваших карт лояльности. Мы не видим ваши карты. Все остается у вас в телефоне."
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        sections.forEach { (title, body) ->
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Text(body, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.85f))
            }
        }
    }
}
