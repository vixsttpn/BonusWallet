
package com.bonuswallet.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Политика конфиденциальности") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("BonusWallet 1.0.0", fontWeight = FontWeight.Bold)
            Text("Дата обновления: 30 августа 2026")

            Text("1. Что такое BonusWallet", fontWeight = FontWeight.Bold)
            Text("BonusWallet - это универсальный кошелек для скидочных, бонусных, клубных и программ лояльности. Приложение предназначено для хранения карт магазинов, ресторанов, супермаркетов, АЗС, кинотеатров, аптек, транспортных карт и других программ лояльности.")

            Text("2. Запрет банковских карт", fontWeight = FontWeight.Bold)
            Text("BonusWallet НЕ предназначен для хранения банковских платежных карт. Пользователю запрещено вводить:\n- банковские карты\n- дебетовые карты\n- кредитные карты\n- виртуальные банковские карты\n- номера банковских счетов\n- CVV/CVC, PIN, пароли\n- платежные реквизиты\n\nBonusWallet не является банковским кошельком, не обрабатывает платежи и не является платежным приложением.")

            Text("3. Обнаружение банковских карт", fontWeight = FontWeight.Bold)
            Text("Приложение предпринимает меры для обнаружения потенциальных платежных карт с использованием локальных алгоритмов: Luhn checksum, длина номера, платежные паттерны (Visa, Mastercard, Amex, UnionPay, JCB, Discover).\n\nЕсли система с высокой вероятностью определила банковскую карту, она НЕ сохраняется в базу данных, не записывается в логи, не отправляется по сети, не сохраняется в backup. Поле ввода очищается.")

            Text("4. Локальное хранение", fontWeight = FontWeight.Bold)
            Text("Все карты хранятся только локально на устройстве в базе данных Room. Мы не отправляем номера карт на сторонние серверы для проверки, кроме случаев когда официальный провайдер предоставляет API (в настоящее время ни один провайдер не предоставляет публичный API для сторонних кошельков, поэтому все проверки - только локальная проверка формата).")

            Text("5. Проверка карт", fontWeight = FontWeight.Bold)
            Text("Приложение честно определяет уровень достоверности:\n- Высокая: подтвержденный официальный формат\n- Средняя: номер соответствует известному формату, но невозможно подтвердить принадлежность\n- Низкая: определить невозможно\n\nМы не показываем \"Карта действительна\" если не смогли реально проверить. Не показываем баланс если не получили его из реального источника.")

            Text("6. Баланс", fontWeight = FontWeight.Bold)
            Text("Если официальный API программы лояльности предоставляет баланс, мы показываем его. В настоящее время Bravo, Wolt, KFC не предоставляют публичный API для сторонних приложений, поэтому BonusWallet показывает \"Баланс недоступен\" или \"Для проверки требуется вход в аккаунт\" честно, без выдуманных чисел.")

            Text("7. Безопасность", fontWeight = FontWeight.Bold)
            Text("Мы не храним ложные данные. Если баланс был получен из реального источника - сохраняем. Если запрос завершился ошибкой - не заменяем старое значение нулем.")

            Text("8. Права", fontWeight = FontWeight.Bold)
            Text("BonusWallet не копирует официальные изображения, логотипы, товарные знаки один в один. Используются нейтральные элементы, вдохновленные цветами категорий, но не выдающие BonusWallet за официальное приложение компании.")
        }
    }
}

@Composable
fun TermsViewScreen(onBack: () -> Unit) {
    PrivacyScreen(onBack = onBack)
}

