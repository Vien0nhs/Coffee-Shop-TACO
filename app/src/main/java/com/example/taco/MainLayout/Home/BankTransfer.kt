package com.example.taco.MainLayout.Home

import CustomerDatabase
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.OrderProduct
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product
import com.example.taco.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CheckoutScreen(navController: NavController) {
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
        CheckoutTopBar(navController)
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
                            CheckoutItemRow(navController, orderProduct = orderProduct, product = product)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CheckoutItemRow(navController: NavController, orderProduct: OrderProduct, product: Product) {
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
                .padding(vertical = 8.dp)
        ) {
            Text(text = product.name , color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier
                    .padding(end = 75.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Price: ${String.format("%.3f", product.price)} VND" + " x(${orderProduct.quantity})",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Tổng: ${String.format("%.3f", orderProduct.totalPrice)} VND",color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier
                    .padding(end = 75.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text =  "Ghi chú: " + orderProduct.note , color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}


// TopBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutTopBar(navController: NavController) {
    val sqlite = CustomerDatabase(LocalContext.current)
    val customers = sqlite.getAllCustomers()
    val name = customers[0].customerName
    val firestore = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope()
    val orderProducts = remember { mutableStateListOf<OrderProduct>() }
    val TotalOrderPrice = remember { mutableStateOf(0.0) }
    val showQRDialog = remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val copyCheck = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            orderProducts.addAll(firestore.getAllOrderProducts())
        }
    }
    val filteredOrderProductsForCustomer = orderProducts.filter { it.phoneNumber == customers[0].customerNumPhone } // Lọc tất cả orderProducts theo tên khách hàng
    if(filteredOrderProductsForCustomer.isNotEmpty()){
        TotalOrderPrice.value =  filteredOrderProductsForCustomer.sumOf { it.totalPrice }
    }
    // Hiển thị dialog mã QR
    if (showQRDialog.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            AlertDialog(
                properties = DialogProperties(
                    dismissOnBackPress = false, // Không cho phép đóng khi nhấn phím back
                    dismissOnClickOutside = false // Không cho phép đóng khi nhấn ngoài dialog
                ),
                onDismissRequest = { showQRDialog.value = false },
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Mã QR Thanh Toán",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hiển thị hình ảnh mã QR từ res/drawable
                        Image(
                            painter = painterResource(id = R.drawable.img_2),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.padding(top = 16.dp))
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Back",
                                tint = Color.Green
                            )
                            Text(
                                text = "Vui lòng thanh toán và đợi nhân viên xác nhận.",
                                color = Color.White,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Button(
                            enabled = copyCheck.value,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(190, 160, 120)
                            ),
                            onClick = {
                                copyCheck.value = false
                                // Sao chép số tài khoản vào clipboard
                                clipboardManager.setText(AnnotatedString("101875143109"))

                                // Hiển thị thông báo Toast
                                Toast.makeText(
                                    navController.context,
                                    "Số tài khoản đã được sao chép!",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(text = "Sao chép số tài khoản")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { showQRDialog.value = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        181,
                                        136,
                                        99
                                    )
                                )
                            ) {
                                Text("Huỷ thanh toán", color = Color.White)
                            }
                            val isPayCheck = filteredOrderProductsForCustomer.any { it.isPayCheck }
                            Button(
                                enabled = false,
                                onClick = {

                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        181,
                                        136,
                                        99
                                    )
                                )
                            ) {
                                if(!isPayCheck){
                                    Text("Chưa thanh toán.", color = Color.White, fontSize = 10.sp)
                                }
                                else{
                                    Text("Đã thanh toán", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                containerColor = Color(181, 136, 99) // Màu nền cho dialog
            )
        }
    }
    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Khách hàng: $name",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "Xác nhận thanh toán: ${String.format("%.3f", TotalOrderPrice.value)} VND",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("cart") }) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = {
                showQRDialog.value = true
            }) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Proceed to Checkout",
                    tint = Color.Green
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(181, 136, 99), // Màu nâu RGB
            titleContentColor = Color.White
        )
    )
}
