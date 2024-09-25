package com.example.taco.MainLayout.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import com.example.taco.Data.DatabaseTACO

@Composable
fun PaymentsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99)),
    ){

    }
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