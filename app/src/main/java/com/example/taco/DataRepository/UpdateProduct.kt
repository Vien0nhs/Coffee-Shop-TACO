//package com.example.taco.Data
//
//import android.content.Context
//import android.graphics.BitmapFactory
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextDecoration
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//
//@Composable
//fun UpdateProductScreen(context: Context, navController: NavController) {
//    val dbhelper = remember { DatabaseTACO(context) }
//    val products = remember { dbhelper.getAllProducts() }
//
//    Column {
//        UpdateProductTopBar(navController)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyColumn {
//            items(products) { product ->
//                UProductRow(product, onUpdate = { productId ->
//                    // Điều hướng đến trang UpdateDetailProduct với ID của sản phẩm
//                    navController.navigate("updateDetail/$productId")
//                })
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UpdateProductTopBar(navController: NavController) {
//    CenterAlignedTopAppBar(
//        title = { Text("Update Product") },
//        navigationIcon = {
//            IconButton(onClick = { navController.navigateUp() }) {
//                Icon(
//                    imageVector = Icons.Default.ArrowBack,
//                    contentDescription = "Back",
//                    tint = Color.White
//                )
//            }
//        },
//        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//            containerColor = Color(181, 136, 99),
//            titleContentColor = Color.White
//        )
//    )
//}
//
//@Composable
//fun UProductRow(product: DatabaseTACO.Product, onUpdate: (Int) -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        // Hiển thị ảnh và thông tin sản phẩm
//        Row(
//            modifier = Modifier.weight(1f),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            product.image?.let {
//                val imageBitmap = BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap()
//                Image(
//                    bitmap = imageBitmap,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(100.dp)
//                        .padding(end = 8.dp)
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
//                    style = MaterialTheme.typography.bodyMedium)
//                product.oldPrice?.let {
//                    Text(
//                        text = "Old Price: ${String.format("%.3f", it)} VND",
//                        style = MaterialTheme.typography.bodyMedium,
//                        textDecoration = TextDecoration.LineThrough
//                    )
//                }
//            }
//        }
//
//        // Nút update sản phẩm
//        Column(
//            modifier = Modifier
//                .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            IconButton(
//                onClick = { onUpdate(product.id) }) {
//                Icon(
//                    imageVector = Icons.Default.Edit,
//                    contentDescription = "Update Product",
//                    tint = Color.Blue
//                )
//            }
//            Text(
//                text = "Chỉnh sửa",
//                style = MaterialTheme.typography.bodySmall,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}
