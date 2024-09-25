package com.example.taco.MainLayout.Home

import CustomerDatabase
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
    // Fetch data from Firebase
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
                                text = "Không có sản phẩm nào trong giỏ hàng của bạn.",
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
    val cusName = remember { mutableStateOf(orderProduct.cusName) }
    val quantity = remember { mutableIntStateOf(orderProduct.quantity) }
    val note = remember { mutableStateOf(orderProduct.note) }
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope() // Tạo coroutine scope

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Display product image
        product.image?.let { imageBase64 ->
            val bitmap = firestoreHelper.base64ToBitmap(imageBase64)
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Display product and order details in columns
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            Text(text = product.name , fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Price: ${String.format("%.3f", product.price)} VND" + " x(${orderProduct.quantity})",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(text = "Tổng giá: ${String.format("%.3f", orderProduct.totalPrice)} VND", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row{
                Text(text = "Xin chào: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${orderProduct.cusName}", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row{
                Text(text = "Ghi chú: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${orderProduct.note}", fontSize = 14.sp)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { showDialog.value = true }
            ) {
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
            IconButton(
                onClick = { showDialog2.value = true }
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveShoppingCart,
                    contentDescription = "Delete Order Product",
                    tint = Color.Red
                )
            }
            Text(
                text = "Xoá",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
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
                                firestoreHelper.deleteOrderProductById(orderProduct.orderId) // Kiểm tra orderId ở đây
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
                    TextButton(
                        onClick = { showDialog2.value = false }
                    ) {
                        Text("Huỷ", color = Color.White)
                    }
                }
            )
        }
        if (showDialog.value) {
            AlertDialog(
                containerColor = Color(181, 136, 99),
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = "Sửa thông tin:") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = "Món: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
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
                            IconButton(onClick = {
                                quantity.value += 1
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Tăng")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = cusName.value,
                            onValueChange = { cusName.value = it },
                            label = { Text("Số bàn", color = Color.Black) },
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Black,
                            )
                        )
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

                            // Tạo đối tượng OrderProduct với các thông tin cần thiết, ngoại trừ orderId
                            val updatedOrderProduct = OrderProduct(
                                orderId = orderProduct.orderId,
                                productId = product.productId,
                                quantity = quantity.value,
                                totalPrice = totalPrice,
                                cusName = cusName.value,
                                note = note.value
                            )

                            coroutineScope.launch {
                                // Sửa OrderProduct trong Firestore
                                firestoreHelper.updateOrderProductById(updatedOrderProduct.orderId, updatedOrderProduct)
                            }
                            navController.navigate("cart")
                            showDialog.value = false
                        }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar(navController: NavController) {
    val sqlite = CustomerDatabase(LocalContext.current)
    val customers = sqlite.getAllCustomers()
    val name = customers[0].customerName
    CenterAlignedTopAppBar( 
        title = {
            Text(
                "Giỏ của bạn $name",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
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
