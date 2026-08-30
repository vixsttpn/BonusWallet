
package com.bonuswallet.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Политика конфиденциальности") }, navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }) }) { pad ->
        androidx.compose.foundation.layout.Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp)) {
            PolicyText()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsViewScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Правила использования") }, navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } }) }) { pad ->
        androidx.compose.foundation.layout.Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp)) {
            PolicyText()
        }
    }
}
