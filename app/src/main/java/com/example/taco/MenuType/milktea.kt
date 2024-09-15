package com.example.taco.MenuType

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taco.FirebaseAPI.FirestoreHelper
import com.example.taco.FirebaseAPI.OrderProduct
import com.example.taco.FirebaseAPI.Product
import com.example.taco.FirebaseAPI.base64ToBitmap
import kotlinx.coroutines.launch

@Composable
fun MilkteaScreen(navController: NavController, context: Context) {
    val firestoreHelper = remember { FirestoreHelper() }
    var products = remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        isLoading = true
        products.value = firestoreHelper.getAllProducts().filter {
            it.name.contains("milktea", ignoreCase = true)
        }
        isLoading = false
    }


    Column {
        MilkteaTopBar(navController)

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            // Show loading indicator while fetching data
            CircularProgressIndicator(
                color = Color(181, 136, 99),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            LazyColumn {
                items(products.value) { product ->
                    HorizontalDivider(
                        color = Color(181, 136, 99),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkteaTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Milktea Products") },
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

//@Composable
//fun MilkteaProductRow(product: Product) {
//    val showDialog = remember { mutableStateOf(false) }
//    val quantity = remember { mutableStateOf(1) }
//    val tablenumber = remember { mutableStateOf("") }
//    val note = remember { mutableStateOf("") }
//    val imageBitmap = product.image?.let { base64ToBitmap(it)?.asImageBitmap() }
//    val firestoreHelper = remember { FirestoreHelper() }
//    val coroutineScope = rememberCoroutineScope() // Tạo coroutine scope
//
//    if (showDialog.value) {
//        AlertDialog(
//            onDismissRequest = { showDialog.value = false },
//            title = { Text(text = "Chọn số lượng và thêm chi tiết") },
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
//                    Spacer(modifier = Modifier.height(16.dp))
//                    OutlinedTextField(
//                        value = tablenumber.value,
//                        onValueChange = { tablenumber.value = it },
//                        label = { Text("Số bàn") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    OutlinedTextField(
//                        value = note.value,
//                        onValueChange = { note.value = it },
//                        label = { Text("Ghi chú") }
//                    )
//                }
//            },
//            confirmButton = {
//                Button(onClick = {
//                    // Tính tổng giá
//                    val totalPrice = product.price * quantity.value
//
//                    // Tạo đối tượng OrderProduct với các thông tin cần thiết, ngoại trừ orderId
//                    val orderProduct = OrderProduct(
//                        orderId = "", // Để trống, Firebase sẽ tự động tạo ID
//                        productId = product.id,
//                        quantity = quantity.value,
//                        totalPrice = totalPrice,
//                        tablenumber = tablenumber.value,
//                        note = note.value
//                    )
//
//                    coroutineScope.launch {
//                        // Thêm OrderProduct vào Firestore
//                        firestoreHelper.addOrderProduct(orderProduct) { documentReference ->
//                            // Khi Firestore đã tạo ID, bạn có thể truy xuất nó từ documentReference
//                            val generatedOrderId = documentReference.id
//                            // Nếu cần thiết, bạn có thể cập nhật lại orderProduct với ID mới này
//                            // orderProduct.orderId = generatedOrderId
//                            // Sau đó, bạn có thể làm các thao tác tiếp theo, ví dụ như điều hướng hoặc thông báo thành công
//                        }
//                    }
//
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
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
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
//                    contentScale = ContentScale.Crop, // Đảm bảo hình ảnh không bị biến dạng
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
//                    style = MaterialTheme.typography.bodyMedium
//                )
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
//                .height(70.dp)
//                .padding(top = 10.dp),
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
