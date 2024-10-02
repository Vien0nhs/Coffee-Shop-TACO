package com.example.taco.MainLayout.Admin

import CustomerDatabase
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Customer
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.OrderProduct
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(navController: NavController) {
    val orderProducts = remember { mutableStateListOf<OrderProduct>() }
    val customers = remember { mutableStateListOf<Customer>() }
    val products = remember { mutableStateListOf<Product>() }
    var isLoading by remember { mutableStateOf(true) }
    val firestoreHelper = remember { FirestoreHelper() }
    var time by remember { mutableStateOf(60) }
    val coroutineScope = rememberCoroutineScope()
    // Get all OrderProduct và Product ở Firebase
    LaunchedEffect(Unit) {
        orderProducts.clear()
        orderProducts.addAll(firestoreHelper.getAllOrderProducts()) // Gọi hàm lấy tất cả OrderProducts từ Firebase

        customers.clear()
        customers.addAll(firestoreHelper.getAllCustomers()) // Gọi hàm lấy tất cả Customers từ Firebase

        products.clear()
        products.addAll(firestoreHelper.getAllProducts()) // Gọi hàm lấy tất cả Products từ Firebase
        isLoading = false

    }

    // Get all OrderProduct và Product từ Firebase
    LaunchedEffect(Unit) {
        while (time > 0){
            delay(1000)
            time--
            if(time == 0){
                isLoading = true
                orderProducts.clear()
                orderProducts.addAll(firestoreHelper.getAllOrderProducts()) // Gọi hàm lấy tất cả OrderProducts từ Firebase

                customers.clear()
                customers.addAll(firestoreHelper.getAllCustomers()) // Gọi hàm lấy tất cả Customers từ Firebase

                products.clear()
                products.addAll(firestoreHelper.getAllProducts()) // Gọi hàm lấy tất cả Products từ Firebase

                isLoading = false
                time = 60
            }
        }
    }


    // Nhóm orderProducts theo phoneNumber
    val groupedOrderProducts = orderProducts.groupBy { it.phoneNumber }

    // Hiển thị theo lazyColumn với Get all đã được gán Id
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
    ) {
        // Top bar
        CenterAlignedTopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Thu ngân",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Thời gian reset trang: $time giây",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("admin") }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Lưu lại",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(181, 136, 99),
                titleContentColor = Color.White
            )
        )


        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            ) {
                if (groupedOrderProducts.isEmpty()) {
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
                                text = "Chưa có đơn hàng.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Lặp qua các nhóm orderProducts
                    items(groupedOrderProducts.keys.toList().sortedBy { groupedOrderProducts.getValue(it).first().isPayCheck }) { phoneNumber ->
                        var orders = groupedOrderProducts[phoneNumber] ?: emptyList()
                        val customer = customers.find { it.phoneNumber == phoneNumber }
                        if (customer != null) {
                            HorizontalDivider(
                                color = Color.White,
                                thickness = 1.dp,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .padding(start = 16.dp, end = 16.dp)
                            )
                            // Chọn order đầu tiên để hiển thị
                            CashierItemRow(navController, orders, customer, products)
                        }
                    }
                }
            }
        }
    }
}


// Hàng hóa đơn
@Composable
fun CashierItemRow(navController: NavController,
                   orderProducts: List<OrderProduct>,
                   customer: Customer,
                   products: List<Product>,
                   ) {
    val showDialog = remember { mutableStateOf(false) }
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope()
    val totalOrderPrice = remember { mutableStateOf(0.0) }
    val sqlite = CustomerDatabase(LocalContext.current)
    val customers = sqlite.getAllCustomers()
    var showCheckDialog by remember { mutableStateOf(false) }

    // Tính tổng giá cho đơn hàng của khách hàng
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Kiểm tra nếu danh sách customers không rỗng
            if (customers.isNotEmpty()) {
                // Kiểm tra nếu có đơn hàng cho khách hàng đó
                if (orderProducts.isNotEmpty()) {
                    // Tính tổng giá cho đơn hàng của khách hàng
                    totalOrderPrice.value = orderProducts.sumOf { it.totalPrice }
                } else {
                    totalOrderPrice.value = 0.0 // Không có đơn hàng, tổng là 0
                }
            } else {
                totalOrderPrice.value = 0.0 // Không có khách hàng, tổng là 0
            }
        }
    }

    // Hiển thị hộp thoại xác nhận thanh toán
    if (showCheckDialog) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnBackPress = false, // Không cho phép đóng khi nhấn phím back
                dismissOnClickOutside = false // Không cho phép đóng khi nhấn ngoài dialog
            ),
            onDismissRequest = { showCheckDialog = false },
            title = { Text(text = "Xác nhận thanh toán đơn của khách ${orderProducts.firstOrNull()?.phoneNumber ?: ""} ", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Chắc chắn khách hàng này đã thanh toán qua chuyển khoản ?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                firestoreHelper.updateOrderProductByFilterList(orderProducts, true)
                                showCheckDialog = false // Reset dialog sau khi hoàn tất
                            } catch (e: Exception) {
                                Log.e("FirestoreError", "Lỗi khi cập nhật trạng thái: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Xác nhận", color = Color.White)
                }
            }
            ,
            dismissButton = {
                Button(
                    onClick = { showCheckDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                ) {
                    Text("Huỷ", color = Color.White)
                }
            },
            containerColor = Color(181, 136, 99), // Màu nâu RGB
            textContentColor = Color.White
        )
    }

    // thông tin khách hàng
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .clickable { showDialog.value = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Khách: ${customer.cusName}",
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SDT: ${customer.phoneNumber}",
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Black, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tổng đơn: ${String.format("%.3f", totalOrderPrice.value)} VND",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
        }


        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .height(75.dp)  // Chiều cao của đường kẻ
                .width(1.dp)    // Độ dày của đường kẻ
                .background(Color.Black)  // Màu sắc của đường kẻ
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (orderProducts.any { it.isPayCheck }) {
                IconButton(
                    enabled = false,
                    onClick = {

                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check Payment Status",
                        tint = Color(34, 139, 34)
                    )
                }
                Text(
                    text = "Đã thanh toán",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color(34, 139, 34)
                )
            } else {
                IconButton(
                    enabled = true,
                    onClick = {
                        showCheckDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check Payment Status",
                        tint = Color.LightGray
                    )
                }
                Text(
                    text = "Xác nhận thanh toán",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.LightGray
                )
            }

        }
    }
    // Hiển thị hộp thoại chi tiết đơn hàng
    if (showDialog.value) {
        OrderDetailsDialog(customer, showDialog, orderProducts, products)
    }
}

// Hộp thoại chi tiết đơn hàng
@Composable
fun OrderDetailsDialog(customer: Customer, showDialog: MutableState<Boolean>, orderProducts: List<OrderProduct>, products: List<Product>) {

    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("Chi tiết đơn hàng của khách ${customer.cusName}") },
        text = {
            LazyColumn {
                items(orderProducts) { orderProduct ->
                    val product = products.find { it.productId == orderProduct.productId }
                    val imageBitmap = product?.image?.let {
                        FirestoreHelper().base64ToBitmap(it)?.asImageBitmap()
                    }
                    Column {
                        product?.let {
                            imageBitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = "Product Image",
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text("Tên sản phẩm: ${product.name}")
                            Text(text = "Price: ${String.format("%.3f", product.price)} VND")
                            Text("Số lượng: ${orderProduct.quantity}")
                            Text("Giá cũ: ${String.format("%.3f", product.oldPrice)} VND")
                            Text("Ghi chú: ${orderProduct.note}")
                            Text("Tổng giá: ${String.format("%.3f", orderProduct.totalPrice)} VND")
                            Text("Trạng thái thanh toán: ${if (orderProduct.isPayCheck) "Đã thanh toán" else "Chưa thanh toán"}")
                        } ?: run {
                            Text("Sản phẩm không tồn tại.")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text("Đóng")
            }
        }
    )
}
