package com.example.taco.MenuType.Cake

import CustomerDatabase
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Customer
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.OrderProduct
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CakeScreen(navController: NavController) {
    val firestoreHelper = remember { FirestoreHelper() }
    var products = remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        products.value = firestoreHelper.getAllProducts().filter {
            it.name.contains("cake", ignoreCase = true)
        }
        isLoading = false
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
    ) {
        CakeTopBar(navController)

        Spacer(modifier = Modifier.height(16.dp))
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
                items(products.value) { product ->

                    HorizontalDivider(
                        color = Color.White,
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    )
                    ProductRow(navController, product)

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CakeTopBar(navController: NavController) {
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope() // Tạo coroutine scope

    CenterAlignedTopAppBar(
        title = { Text("Cake Products") },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {  // Thêm phần này để định nghĩa các biểu tượng bên phải
            Row {

                IconButton(onClick = { navController.navigate("cart") }) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart, // Biểu tượng giỏ hàng
                        contentDescription = "Cart",
                        tint = Color.White
                    )
                }
                Text(
                    "($)",
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun ProductRow(navController: NavController, product: Product) {
    val showDialog = remember { mutableStateOf(false) }
    val quantity = remember { mutableStateOf(1) }
    val note = remember { mutableStateOf("") }
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope() // Tạo coroutine scope
    val imageBitmap = product.image?.let { firestoreHelper.base64ToBitmap(it)?.asImageBitmap() }
    val sqlite = CustomerDatabase(LocalContext.current)
    val customers = sqlite.getAllCustomers()[0]
    var name = customers.customerName
    var phoneNumber = customers.customerNumPhone


    if (showDialog.value) {
        AlertDialog(
            containerColor = Color(181, 136, 99),
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Thêm thông tin:") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Món: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Số lượng:")
                        IconButton(onClick = { if (quantity.value > 1) quantity.value -= 1 }) {
                            Icon(Icons.Default.Remove, contentDescription = "Giảm")
                        }
                        Text(
                            text = quantity.value.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { quantity.value += 1 }) {
                            Icon(Icons.Default.Add, contentDescription = "Tăng")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = note.value,
                        onValueChange = { note.value = it },
                        label = { Text("Ghi chú", color = Color.Black) },
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Black,
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99)),
                    onClick = {
                        coroutineScope.launch {
                            val totalPrice = product.price * quantity.value
                            val newOrderProduct = OrderProduct(
                                // Tạo các Đơn hàng mới vào API:
                                orderId = "", // Lưu Id tự động OrderProduct mới.
                                productId = product.productId, // Lưu Id Product hiện tại.
                                cusName = name, // Lưu tên khách hàng cục bộ.
                                phoneNumber = phoneNumber, // Lưu số điện thoại cục bộ.
                                isProblem = false, // Lưu ban đầu đơn hàng chưa có vấn đề ở phía pha chế.
                                quantity = quantity.value, // Lưu số lượng sản phẩm.
                                totalPrice = totalPrice, // Lưu tổng tiền.
                                note = note.value, // Lưu ghi chú của khách về món.
                                orderDone = false, // Lưu đơn hàng chưa được giao.
                                startOrderTime = Date(), // Lưu thời gian bắt đầu đặt hàng.
                                // orderCompletedTime = ... // Người pha chế sẽ lưu thời gian hoàn thành món tại quán.
                            )
                            firestoreHelper.addOrderProduct(newOrderProduct) { documentReference ->
                                // Xử lý sau khi thêm vào Firestore
                            }
                        }
                        showDialog.value = false
                    }
                ) {
                    Text("Thêm vào giỏ của bạn")
                }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99)),
                    onClick = { showDialog.value = false }
                ) {
                    Text("Huỷ")
                }
            }
        )
    }


    // Code hiển thị sản phẩm
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageBitmap?.let {
                Image(bitmap = it, contentDescription = "Product Image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Price: ${String.format("%.3f", product.price)} VND", style = MaterialTheme.typography.bodyMedium)
                product.oldPrice?.let {
                    Text(
                        text = "Old Price: ${String.format("%.3f", it)} VND",
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .height(70.dp)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { showDialog.value = true }) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Order Product",
                    tint = Color.Green
                )
            }
            Text(text = "Thêm", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}


