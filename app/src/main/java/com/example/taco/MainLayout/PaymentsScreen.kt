package com.example.taco.MainLayout

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PaymentsScreen() {
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsTopBar(){
    CenterAlignedTopAppBar(
        title = { Text("Payments") },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99),
            titleContentColor = Color.White
        )
    )
}