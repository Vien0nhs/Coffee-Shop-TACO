package com.example.taco
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taco.MainLayout.Admin.Product.AddProductScreen
//import com.example.taco.Data.DeleteProductScreen
import com.example.taco.MainLayout.Admin.Product.UpdateDetailProductScreen
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
//import com.example.taco.Data.UpdateProductScreen
import com.example.taco.MainLayout.MenuType.Cake.CakeScreen
import com.example.taco.MainLayout.MenuType.Coffee.CoffeeScreen
import com.example.taco.MainLayout.MenuType.Juices.JuiceScreen
import com.example.taco.MainLayout.MenuType.Milktea.MilkteaScreen
import com.example.taco.MainLayout.MenuType.Drinks.OtherDrinksScreen
import com.example.taco.MainLayout.Admin.AdminScreen
import com.example.taco.MainLayout.Admin.CashierScreen
import com.example.taco.MainLayout.Home.CartScreen
import com.example.taco.MainLayout.Home.CheckoutScreen
import com.example.taco.MainLayout.Home.HomeScreen
import com.example.taco.MainLayout.Login.LoginScreen
import com.example.taco.MainLayout.Home.PaymentsScreen
import com.example.taco.MainLayout.Home.SearchScreen
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirebaseApp.initializeApp(this)
            Nav()
            loadDataFromFirestore()
        }
    }
    private val firestoreHelper = FirestoreHelper()

    private fun loadDataFromFirestore() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Lấy tất cả sản phẩm
                val products = firestoreHelper.getAllProducts()

                // Lấy tất cả tài khoản

                // Lấy tất cả bàn

                // Lấy tất cả đơn hàng (nếu cần)
                val orderProducts = firestoreHelper.getAllOrderProducts()

                // Bạn có thể lưu trữ dữ liệu vào biến hoặc xử lý theo cách bạn muốn

            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching data: ${e.message}")
                // Hiển thị thông báo lỗi cho người dùng nếu cần
            }
        }
    }
}

@Composable
fun Nav(){
    val navController = rememberNavController()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = "login") {
        composable("home") { HomeScreen(navController, drawerState, scope, context) }
        composable("pay") { PaymentsScreen() }
        composable("admin") { AdminScreen(navController, context) }
        composable("login") { LoginScreen(navController, ) }
        composable("cart") { CartScreen(navController) }
        composable("add") { AddProductScreen(navController, context) }
        composable("checkout") { CheckoutScreen(navController) }
        composable("search") { SearchScreen(navController, context) }
        composable("Cashier") { CashierScreen(navController) }
//        composable("addtable") { AddTableScreen(navController, context) }
//        composable("delete") { DeleteProductScreen(navController, context) }
//        composable("update") { UpdateProductScreen(context, navController) }
        // Route cho UpdateDetailProductScreen, nhận productId làm tham số
        composable("updateDetail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UpdateDetailProductScreen(navController, productId, context)
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