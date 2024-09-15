package com.example.taco.DataRepository

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.taco.FirebaseAPI.FirestoreHelper
import com.example.taco.FirebaseAPI.Product

@Composable
fun UpdateDetailProductScreen(navController: NavController, productId: String) {
    // Sử dụng FirestoreHelper thay vì DatabaseTACO
    val firestoreHelper = remember { FirestoreHelper() }
    var product by remember { mutableStateOf<Product?>(null) }

    // Khai báo các biến trạng thái cho các trường của sản phẩm
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var oldPrice by remember { mutableStateOf(TextFieldValue("")) }

    var showDialog by remember { mutableStateOf(false) } // Biến trạng thái để kiểm soát hiển thị của AlertDialog

    // Chỉ khởi tạo các biến trạng thái một lần khi composable được tạo lần đầu
    LaunchedEffect(productId) {
        product = firestoreHelper.getProductById(productId)
        product?.let {
            name = TextFieldValue(it.name)
            price = TextFieldValue(it.price.toString())
            oldPrice = TextFieldValue(it.oldPrice?.toString() ?: "")
        } ?: run {
            // Xử lý khi sản phẩm là null, ví dụ quay lại màn hình trước đó
            navController.navigate("admin")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        UpdateDetailProductTopBar(navController)

        Spacer(modifier = Modifier.height(16.dp))

        // TextFields for Product properties
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = oldPrice,
            onValueChange = { oldPrice = it },
            label = { Text("Old Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Update Button
        Button(
            onClick = {
                showDialog = true // Hiển thị hộp thoại xác nhận khi nhấn nút "Update"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
        ) {
            Text("Update")
        }

        // Hiển thị AlertDialog khi showDialog = true
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Xác nhận cập nhật") },
                text = { Text("Bạn có chắc chắn muốn cập nhật thông tin sản phẩm này?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Cập nhật sản phẩm trong Firestore
                            product?.let {
                                firestoreHelper.updateProductById(
                                    productId = it.productId,
                                    updatedProduct = Product(
                                        productId = it.productId,
                                        name = name.text,
                                        price = price.text.toDoubleOrNull() ?: 0.0,
                                        oldPrice = oldPrice.text.toDoubleOrNull(),
                                        image = it.image
                                    )
                                )
                            }
                            // Đóng hộp thoại và quay lại màn hình trước đó
                            showDialog = false
                            navController.navigate("admin")
                        }
                    ) {
                        Text("Chấp nhận")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("Hủy bỏ")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDetailProductTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Update Detail Product") },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99),
            titleContentColor = Color.White
        )
    )
}
