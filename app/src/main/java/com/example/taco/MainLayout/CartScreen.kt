package com.example.taco.MainLayout

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.DatabaseTACO
import com.example.taco.FirebaseAPI.FirestoreHelper
import com.example.taco.FirebaseAPI.OrderProduct
import com.example.taco.FirebaseAPI.Product
import com.example.taco.FirebaseAPI.base64ToBitmap
import com.example.taco.R
import com.example.taco.MainLayout.GlobalLoginVariables.passWord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CartScreen(navController: NavController) {
    val dbHelper = FirebaseFirestore.getInstance()
    val orderProducts = remember { mutableStateListOf<OrderProduct>() }
    val products = remember { mutableStateListOf<Product>() }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data from Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        // Fetch OrderProducts
        val orderProductSnapshot = dbHelper.collection("OrderProduct").get().await()
        orderProductSnapshot.documents.mapNotNullTo(orderProducts) { document ->
            document.toObject(OrderProduct::class.java)?.copy(orderId = document.id)
        }

        // Fetch Products
        val productSnapshot = dbHelper.collection("Product").get().await()
        productSnapshot.documents.mapNotNullTo(products) { document ->
            document.toObject(Product::class.java)?.copy(productId = document.id)
        }
        isLoading = false
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            // Show loading indicator while fetching data
            CircularProgressIndicator(
                color = Color(181, 136, 99),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            val orderProduct = orderProducts.find { it.productId == products.firstOrNull()?.productId }
            if(orderProduct == null){
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Không có sản phẩm nào trong giỏ hàng",fontWeight = FontWeight.Bold, fontSize = 14.sp)

                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(orderProducts) { orderProduct ->
                    // Find the corresponding product for this OrderProduct
                    val product = products.find { it.productId == orderProduct.productId }
                    if (product != null) {
                        HorizontalDivider(
                            color = Color(181, 136, 99),
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

@Composable
fun CartItemRow(navController: NavController, orderProduct: OrderProduct, product: Product) {
    val showDialog = remember { mutableStateOf(false) }
    val showDialog2 = remember { mutableStateOf(false) }
    val quantity = remember { mutableStateOf(orderProduct.quantity) }
    val tablenumber = remember { mutableStateOf(orderProduct.tablenumber) }
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
            val bitmap = base64ToBitmap(imageBase64)
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

            Text(text = "Tổng giá: ${String.format("%.3f",orderProduct.totalPrice)} VND", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row{
                Text(text = "Bàn: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${orderProduct.tablenumber}", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row{
                Text(text = "Ghi chú: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${orderProduct.note}", color = Color(181, 136, 99), fontSize = 14.sp)
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
                    tint = Color.Green
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
                    imageVector = Icons.Default.Delete,
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
                onDismissRequest = { showDialog2.value = false },
                title = { Text("Xác nhận xoá") },
                text = { Text("Bạn có chắc chắn xoá món này khỏi giỏ hàng?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Call the Firestore delete method
                            firestoreHelper.deleteOrderProductById(orderProduct.orderId)
                            showDialog2.value = false
                            // Optionally, trigger a refresh of the product list
                            navController.navigate("home")
                        }
                    ) {
                        Text("Xác nhận")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog2.value = false }
                    ) {
                        Text("Huỷ")
                    }
                }
            )
        }
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = "Sửa thông tin:") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Món: ${product.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color =Color(181, 136, 99) )
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
                            value = tablenumber.value,
                            onValueChange = { tablenumber.value = it },
                            label = { Text("Số bàn") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = note.value,
                            onValueChange = { note.value = it },
                            label = { Text("Ghi chú") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Tính tổng giá
                        val totalPrice = product.price * quantity.value

                        // Tạo đối tượng OrderProduct với các thông tin cần thiết, ngoại trừ orderId
                        val orderProduct = OrderProduct(
                            orderId = orderProduct.orderId,
                            productId = product.productId,
                            quantity = quantity.value,
                            totalPrice = totalPrice,
                            tablenumber = tablenumber.value,
                            note = note.value
                        )

                        coroutineScope.launch {
                            // Sửa OrderProduct trong Firestore
                            firestoreHelper.updateOrderProductById(orderProduct.orderId, orderProduct)
                        }
                        navController.navigate("home")
                        showDialog.value = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Cart") },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99),
            titleContentColor = Color.White
        )
    )
}