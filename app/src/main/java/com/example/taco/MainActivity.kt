package com.example.taco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taco.DataRepository.AddProductScreen
import com.example.taco.DataRepository.DatabaseTACO
//import com.example.taco.Data.DeleteProductScreen
import com.example.taco.DataRepository.UpdateDetailProductScreen
import com.example.taco.FirebaseAPI.FirestoreHelper
//import com.example.taco.Data.UpdateProductScreen
import com.example.taco.MenuType.CakeScreen
import com.example.taco.MenuType.CoffeeScreen
import com.example.taco.MenuType.JuiceScreen
import com.example.taco.MenuType.MilkteaScreen
import com.example.taco.MenuType.OtherDrinksScreen
import com.example.taco.MainLayout.AdminScreen
import com.example.taco.MainLayout.CartScreen
import com.example.taco.MainLayout.HomeScreen
import com.example.taco.MainLayout.LoginScreen
import com.example.taco.MainLayout.PaymentsScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseApp.initializeApp(this)
            Nav()
        }
    }
}

@Composable
fun Nav(){
    val dbhelper = DatabaseTACO(LocalContext.current)
    val navController = rememberNavController()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, drawerState, scope, context) }
        composable("pay") { PaymentsScreen() }
        composable("admin") { AdminScreen(navController, context) }
        composable("login") { LoginScreen(navController) }
        composable("cart") { CartScreen(navController) }
        composable("add") { AddProductScreen(navController, context) }
//        composable("delete") { DeleteProductScreen(navController, context) }
//        composable("update") { UpdateProductScreen(context, navController) }
        // Route cho UpdateDetailProductScreen, nhận productId làm tham số
        composable("updateDetail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UpdateDetailProductScreen(navController, productId)
        }

        composable("cake"){
            CakeScreen(navController)
        }
        composable("coffee"){
            CoffeeScreen(navController, context)
        }
        composable("juice"){
            JuiceScreen(navController, context)
        }
        composable("milktea"){
            MilkteaScreen(navController, context)
        }
        composable("other"){
            OtherDrinksScreen(navController, context)
        }
    }
}