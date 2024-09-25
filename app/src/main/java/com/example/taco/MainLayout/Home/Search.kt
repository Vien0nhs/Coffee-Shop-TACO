package com.example.taco.MainLayout.Home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
//import com.example.taco.Data.DatabaseTACO
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product
import com.example.taco.MenuType.Cake.ProductRow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, context: Context) {
    // Create an instance of FirestoreHelper
    val firestoreHelper = remember { FirestoreHelper() }

    // State to hold the list of products
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch products from Firestore
    LaunchedEffect(searchQuery) {
        isLoading = true
        products = firestoreHelper.getAllProducts().filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color(190, 160, 120),
                    unfocusedBorderColor = Color.White,
                ),
                label = { Text("Tìm kiếm sản phẩm", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            // Show loading indicator while fetching data
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            LazyColumn {
                items(products) { product ->
                    HorizontalDivider(
                        color = Color.White,
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    )
                    ProductRow(navController,product)
                }
            }
        }
    }
}
//@Composable
//fun ProductRow(
//    product: Product,
//) {
//    val showDialog = remember { mutableStateOf(false) }
//    val quantity = remember { mutableStateOf(1) }
//    val imageBitmap = product.image?.let { base64ToBitmap(it)?.asImageBitmap() }
//    if (showDialog.value) {
//        AlertDialog(
//            onDismissRequest = { showDialog.value = false },
//            title = { Text(text = "Chọn số lượng") },
//            text = {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(text = "Món: ${product.name}")
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        IconButton(onClick = {
//                            if (quantity.value > 1) quantity.value -= 1
//                        }) {
//                            Icon(Icons.Default.Remove, contentDescription = "Giảm")
//                        }
//                        Text(
//                            text = quantity.value.toString(),
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(horizontal = 16.dp)
//                        )
//                        IconButton(onClick = {
//                            quantity.value += 1
//                        }) {
//                            Icon(Icons.Default.Add, contentDescription = "Tăng")
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                Button(onClick = {
//                    // Tính TotalPrice
//                    val totalPrice = product.price * quantity.value
//                    // Thêm vào bảng OrderProduct
//                    // dbhelper.addOrderProduct(...) // Lưu vào cơ sở dữ liệu của bạn nếu cần
//                    showDialog.value = false
//                }) {
//                    Text("OK")
//                }
//            },
//            dismissButton = {
//                Button(onClick = { showDialog.value = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Row(
//            modifier = Modifier.weight(1f),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            imageBitmap?.let {
//                Image(bitmap = it, contentDescription = "Product Image",
//                    modifier = Modifier
//                        .size(70.dp)
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop,
//                )
//            }
//
//            Column(
//                modifier = Modifier
//                    .padding(16.dp)
//            ) {
//                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
//                Text(
//                    text = "Price: ${String.format("%.3f", product.price)} VND",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontSize = 13.sp
//                )
//                product.oldPrice?.let {
//                    Text(
//                        text = "Old Price: ${String.format("%.3f", it)} VND",
//                        style = MaterialTheme.typography.bodyMedium,
//                        textDecoration = TextDecoration.LineThrough,
//                        fontSize = 13.sp
//                    )
//                }
//            }
//        }
//        Column(
//            modifier = Modifier
//                .height(70.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = { showDialog.value = true }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.AddShoppingCart,
//                    contentDescription = "Order Product",
//                    tint = Color.Green
//                )
//            }
//            Text(
//                text = "Thêm",
//                style = MaterialTheme.typography.bodySmall,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Search") },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99),
            titleContentColor = Color.White
        )
    )
}