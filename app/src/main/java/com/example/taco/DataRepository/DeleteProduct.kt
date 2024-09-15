//package com.example.taco.Data
//
//import android.content.Context
//import android.graphics.BitmapFactory
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextDecoration
//import androidx.navigation.NavController
//
//@Composable
//fun DeleteProductScreen(navController: NavController, context: Context) {
//    val dbhelper = remember { DatabaseTACO(context) }
//    val products = remember { mutableStateListOf(*dbhelper.getAllProducts().toTypedArray()) }
//
//    Column {
//        DeleteProductTopBar(navController)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyColumn {
//            items(products) { product ->
//                ProductRow(product, onDelete = { productId ->
//                    dbhelper.deleteProductById(productId)
//                    products.removeIf { it.id == productId }  // Cập nhật danh sách sản phẩm sau khi xóa
//                })
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//        }
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DeleteProductTopBar(navController: NavController) {
//    CenterAlignedTopAppBar(
//        title = { Text("Delete Product") },
//        navigationIcon = {
//            IconButton(onClick = { navController.navigateUp() }) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
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
//fun ProductRow(product: DatabaseTACO.Product, onDelete: (Int) -> Unit) {
//    var showDialog = remember { mutableStateOf(false) }
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
//
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
//        Column(
//            modifier = Modifier
//                .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Nút xóa sản phẩm
//            IconButton(
//                onClick = { showDialog.value = true }) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Delete Product",
//                    tint = Color.Red
//                )
//            }
//            Text(
//                text = "Xoá",
//                style = MaterialTheme.typography.bodySmall,
//                textAlign = TextAlign.Center
//            )
//        }
//
//    }
//    if (showDialog.value) {
//        AlertDialog(
//            onDismissRequest = { showDialog.value = false },
//            title = { Text("Xác nhận xoá") },
//            text = { Text("Bạn có chắc chắn xoá sản phẩm này khỏi hệ thống?") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        onDelete(product.id) // Gọi hàm onDelete để xóa sản phẩm
//                        showDialog.value = false
//                    }
//                ) {
//                    Text("Chấp nhận")
//                }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = { showDialog.value = false }
//                ) {
//                    Text("Hủy bỏ")
//                }
//            }
//        )
//    }
//}
//
