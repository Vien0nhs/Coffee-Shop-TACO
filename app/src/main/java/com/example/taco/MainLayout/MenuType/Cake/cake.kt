package com.example.taco.MainLayout.MenuType.Cake

import CustomerDatabase
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.window.DialogProperties
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
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val orderProducts = remember { mutableStateListOf<OrderProduct>() }
    LaunchedEffect(Unit) {
        isLoading = true
        products = firestoreHelper.getAllProducts().filter {
            it.name.contains("cake", ignoreCase = true)
        }

        orderProducts.clear()
        orderProducts.addAll(firestoreHelper.getAllOrderProducts())
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
            if (products.isEmpty()) {
                Text(
                    text = "Chưa có sản phẩm nào trong hệ thống.",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(products) { product ->
                        orderProducts.filter { it.productId == product.productId }
                        HorizontalDivider(
                            color = Color.White,
                            thickness = 1.dp,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .padding(start = 16.dp, end = 16.dp)
                        )
                        ProductRow(navController, product, orderProducts)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CakeTopBar(navController: NavController) {

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

        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun ProductRow(navController: NavController, product: Product, orderProduct: List<OrderProduct>) {
    val showDialog = remember { mutableStateOf(false) } // Biến điều khiển hộp thoại thêm sản phẩm
    val quantity = remember { mutableStateOf(1) }
    val note = remember { mutableStateOf("") }
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope()
    val productName = remember { mutableStateOf(product.name) }
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val sqlite = CustomerDatabase(LocalContext.current)
    val context = LocalContext.current
    val customers = sqlite.getAllCustomers()[0]
    val name = customers.customerName
    val phoneNumber = customers.customerNumPhone
    val showDialogCart = remember { mutableStateOf(false) } // Biến điều khiển hộp thoại thành công
    val orderCount = remember { mutableStateOf(0) } // Biến để lưu số lượng sản phẩm trong giỏ hàng

    // Load product image from Firebase Storage
    LaunchedEffect(product.image) {
        product.image?.let { imageUrl ->
            firestoreHelper.loadImageFromStorage(imageUrl, context) { bitmap ->
                currentImageBitmap = bitmap
            }
        }
    }

    // Hiển thị hộp thoại thêm vào giỏ hàng
    if (showDialog.value) {
        AlertDialog(
            containerColor = Color(181, 136, 99),
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "Thêm thông tin:") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Món: ${productName.value}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
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
                            val already = firestoreHelper.getAllOrderProducts().filter {
                                it.phoneNumber == phoneNumber && it.productId == product.productId
                            }
                            if (already.isNotEmpty()) {
                                productName.value = already.first().productId
                                val updateOrderProduct = OrderProduct(
                                    orderId = already.first().orderId,
                                    productId = already.first().productId,
                                    cusName = already.first().cusName,
                                    phoneNumber = already.first().phoneNumber,
                                    isProblem = already.first().isProblem,
                                    quantity = already.first().quantity + quantity.value,
                                    totalPrice = already.first().totalPrice + totalPrice,
                                    note = already.first().note + ", " + note.value,
                                    orderDone = already.first().orderDone,
                                    isPayCheck = already.first().isPayCheck,
                                    startOrderTime = already.first().startOrderTime ?: Date()
                                )
                                firestoreHelper.updateOrderProductById(already.first().orderId, updateOrderProduct)
                                orderCount.value = firestoreHelper.getAllOrderProducts().count { it.phoneNumber == phoneNumber }
                            } else {
                                val newOrderProduct = OrderProduct(
                                    orderId = "",
                                    productId = product.productId,
                                    cusName = name,
                                    phoneNumber = phoneNumber,
                                    isProblem = false,
                                    quantity = quantity.value,
                                    totalPrice = totalPrice,
                                    note = note.value,
                                    orderDone = false,
                                    isPayCheck = false,
                                    startOrderTime = Date()
                                )
                                firestoreHelper.addOrderProduct(newOrderProduct) { documentReference ->
                                    // Xử lý sau khi thêm vào Firestore
                                }
                                orderCount.value = firestoreHelper.getAllOrderProducts().count { it.phoneNumber == phoneNumber }
                            }
                        }
                        showDialog.value = false
                        showDialogCart.value = true
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

    // Hiển thị thông báo thành công khi thêm sản phẩm
    if (showDialogCart.value) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnBackPress = false, // Không cho phép đóng khi nhấn phím back
                dismissOnClickOutside = false // Không cho phép đóng khi nhấn ngoài dialog
            ),
            containerColor = Color(181, 136, 99), // Nền màu xanh lá
            onDismissRequest = { showDialogCart.value = false },
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color.Green,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Thành công!", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sản phẩm trong giỏ: ${orderCount.value}",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    HorizontalDivider(
                        color = Color.White,
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            // Điều hướng tới màn hình giỏ hàng
                            navController.navigate("cart")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                    ) {
                        Text(
                            text = "Đi tới giỏ hàng của bạn",
                            color = Color.White // Màu xanh lá cho text
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Đóng hộp thoại và tiếp tục order
                            showDialogCart.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                    ) {
                        Text(
                            text = "Tiếp tục Order",
                            color = Color.White // Màu xanh lá cho text
                        )
                    }
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
            // Hiển thị hình ảnh sản phẩm từ Firebase Storage (nếu có)
            currentImageBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.White, fontSize = 10.sp)
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
                    tint = Color.White
                )
            }
            Text(text = "Thêm", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}






