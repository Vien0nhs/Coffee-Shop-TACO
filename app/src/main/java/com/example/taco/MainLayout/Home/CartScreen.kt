package com.example.taco.MainLayout.Home

import CustomerDatabase
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.OrderProduct
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.internal.notifyAll

@Composable
fun CartScreen(navController: NavController) {
    val dbHelper = FirebaseFirestore.getInstance()
    val orderProducts = remember { mutableStateListOf<OrderProduct>() }
    val products = remember { mutableStateListOf<Product>() }
    var isLoading by remember { mutableStateOf(true) }
    val firestoreHelper = remember { FirestoreHelper() }
    val sqlite = CustomerDatabase(LocalContext.current)
    val customers = sqlite.getAllCustomers()[0]
    // Get all OrderProduct và Product ở Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        orderProducts.clear()
        orderProducts.addAll(firestoreHelper.getAllOrderProducts()) // Gọi hàm lấy tất cả OrderProducts từ Firebase
        // Fetch Products
        val productSnapshot = dbHelper.collection("Product").get().await()
        productSnapshot.documents.mapNotNullTo(products) { document ->
            document.toObject(Product::class.java)?.copy(productId = document.id)
        }
        isLoading = false
    }
    // Hiển thị theo lazyColumn với Get all đã được gán Id
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
    ) {
        CartTopBar(navController)
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            val filteredOrderProducts = orderProducts.filter { it.phoneNumber == customers.customerNumPhone } // Lọc tất cả orderProducts theo tên khách hàng

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                if (filteredOrderProducts.isEmpty()) {
                    // Nếu không có sản phẩm nào
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 300.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bạn sẽ thấy các món đã đặt tại đây.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(filteredOrderProducts) { orderProduct ->
                        val product = products.find { it.productId == orderProduct.productId }
                        if (product != null) {
                            HorizontalDivider(
                                color = Color.White,
                                thickness = 1.dp,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .padding(start = 16.dp, end = 16.dp)
                            )
                            CartItemRow(navController, orderProduct = orderProduct, product = product)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CartItemRow(navController: NavController, orderProduct: OrderProduct, product: Product) {
    val showDialog = remember { mutableStateOf(false) }
    val showDialog2 = remember { mutableStateOf(false) }
    val quantity = remember { mutableIntStateOf(orderProduct.quantity) }
    val note = remember { mutableStateOf(orderProduct.note) }
    val firestoreHelper = remember { FirestoreHelper() }
    val sqlite = CustomerDatabase(LocalContext.current)
    val context = LocalContext.current
    val customers = sqlite.getAllCustomers()
    val name = customers[0].customerName
    val phone = customers[0].customerNumPhone
    val coroutineScope = rememberCoroutineScope() // Tạo coroutine scope
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load product image from Firebase Storage
    LaunchedEffect(product.image) {
        product.image?.let { imageUrl ->
            firestoreHelper.loadImageFromStorage(imageUrl, context) { bitmap ->
                currentImageBitmap = bitmap
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Hiển thị các sản phẩm trong giỏ
        Row {
            // Hiển thị hình ảnh sản phẩm từ Firebase Storage (nếu có)
            currentImageBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Hiển thị thông tin sản phẩm và chi tiết đơn hàng
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(end = 16.dp)
                )
                Text(
                    text = "Price: ${String.format("%.3f", product.price)} VND" + " x(${orderProduct.quantity})",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Tổng giá: ${String.format("%.3f", orderProduct.totalPrice)} VND",
                    color = Color.Black,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(end = 16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Ghi chú: ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${orderProduct.note}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { showDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Order Product",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Sửa",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { showDialog2.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Order Product",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Xoá",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Hiển thị hộp thoại xoá
        if (showDialog2.value) {
            AlertDialog(
                containerColor = Color(181, 136, 99),
                onDismissRequest = { showDialog2.value = false },
                title = { Text("Xác nhận xoá") },
                text = { Text("Bạn có chắc chắn xoá món này khỏi giỏ hàng?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Call the Firestore delete method
                            coroutineScope.launch {
                                firestoreHelper.deleteOrderProductById(orderProduct.orderId)
                            }
                            showDialog2.value = false
                            // Optionally, trigger a refresh of the product list
                            navController.navigate("cart")
                        }
                    ) {
                        Text("Xác nhận", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog2.value = false }) {
                        Text("Huỷ", color = Color.White)
                    }
                }
            )
        }

        // Hiển thị hộp thoại sửa
        if (showDialog.value) {
            AlertDialog(
                containerColor = Color(181, 136, 99),
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = "Sửa thông tin:") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Món: ${product.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = {
                                if (quantity.value > 1) quantity.value -= 1
                            }) {
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
                            // Tính tổng giá
                            val totalPrice = product.price * quantity.value

                            // Tạo đối tượng OrderProduct với các thông tin cần thiết
                            val updatedOrderProduct = OrderProduct(
                                orderId = orderProduct.orderId,
                                productId = product.productId,
                                cusName = name,
                                phoneNumber = phone,
                                quantity = quantity.value,
                                totalPrice = totalPrice,
                                note = note.value
                            )
                            coroutineScope.launch {
                                // Sửa OrderProduct trong Firestore
                                firestoreHelper.updateOrderProductById(updatedOrderProduct.orderId, updatedOrderProduct)
                            }
                            navController.navigate("cart")
                            showDialog.value = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99)),
                        onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


// TopBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar(navController: NavController) {
    val sqlite = CustomerDatabase(LocalContext.current)
    val customers = sqlite.getAllCustomers()
    val name = customers[0].customerName
    val phone = customers[0].customerNumPhone
    val firestore = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope()

    val showDeleteDialog = remember { mutableStateOf(false) }  // Control Dialog xoá tất cả
    val showCheckOutDialog = remember { mutableStateOf(false) } // Control Dialog xác nhận thanh toán
    val showEmptyCartDialog = remember { mutableStateOf(false) } // Control Dialog giỏ hàng trống


    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Giỏ của: $name",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(onClick = {
                    coroutineScope.launch {
                        val filter = firestore.getAllOrderProducts().find { it.phoneNumber == phone }
                        if(filter == null){
                            showCheckOutDialog.value = false
                            showEmptyCartDialog.value = true
                        }
                        else{
                            showCheckOutDialog.value = true
                            showEmptyCartDialog.value = false
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Proceed to Checkout",
                        tint = Color.White
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = {
                showDeleteDialog.value = true // Hiển thị dialog khi click vào delete
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Cart",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99), // Màu nâu RGB
            titleContentColor = Color.White
        )
    )
    // Dialog xác nhận giỏ hàng trống
    if (showEmptyCartDialog.value) {
        AlertDialog(
            onDismissRequest = { showEmptyCartDialog.value = false },
            title = { Text(text = "Giỏ hàng trống", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn không có sản phẩm nào trong giỏ hàng!", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = { showEmptyCartDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Đóng", color = Color.White)
                }
            },
            containerColor = Color(181, 136, 99), // Màu nâu RGB
            textContentColor = Color.White
        )
    }
    // Dialog xác nhận xoá tất cả món trong giỏ
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(text = "Xoá tất cả sản phẩm", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn có chắc chắn muốn xoá tất cả sản phẩm khỏi giỏ hàng?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            firestore.deleteOrderProductsByPhoneNumber(phone)
                            firestore.getAllOrderProducts()
                            navController.navigate("cart") // Cập nhật lại giỏ hàng
                        }
                        showDeleteDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Xoá", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Huỷ", color = Color.White)
                }
            },
            containerColor = Color(181, 136, 99), // Màu nâu RGB
            textContentColor = Color.White
        )
    }

    // Dialog xác nhận điều hướng tới trang thanh toán
    if (showCheckOutDialog.value) {
        AlertDialog(
            onDismissRequest = { showCheckOutDialog.value = false },
            title = { Text(text = "Xác nhận thanh toán", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn có chắc chắn muốn tiếp tục tới trang thanh toán?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        // Điều hướng tới trang thanh toán (sẽ viết sau)
                        navController.navigate("checkout")
                        showCheckOutDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Xác nhận", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCheckOutDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Huỷ", color = Color.White)
                }
            },
            containerColor = Color(181, 136, 99), // Màu nâu RGB
            textContentColor = Color.White
        )
    }
}
