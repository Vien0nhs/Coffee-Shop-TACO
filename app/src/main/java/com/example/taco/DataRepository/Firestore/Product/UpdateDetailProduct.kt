package com.example.taco.DataRepository.Firestore.Product

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product

@Composable
fun UpdateDetailProductScreen(navController: NavController, productId: String, context: Context) {
    // Sử dụng FirestoreHelper thay vì DatabaseTACO
    val firestoreHelper = remember { FirestoreHelper() }
    var product by remember { mutableStateOf<Product?>(null) }

    // Khai báo các biến trạng thái cho các trường của sản phẩm
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var oldPrice by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) } // Biến trạng thái để kiểm soát hiển thị của AlertDialog
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Lưu trữ URI của hình ảnh mới
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) } // Lưu trữ Bitmap của hình ảnh hiện tại
    // Chỉ khởi tạo các biến trạng thái một lần khi composable được tạo lần đầu
    LaunchedEffect(productId) {
        product = firestoreHelper.getProductById(productId)
        product?.let {
            name = TextFieldValue(it.name)
            price = TextFieldValue(it.price.toString())
            oldPrice = TextFieldValue(it.oldPrice?.toString() ?: "")
            currentImageBitmap = it.image?.let { firestoreHelper.base64ToBitmap(it) } // Chuyển đổi từ Base64
        } ?: run {
            // Xử lý khi sản phẩm là null, ví dụ quay lại màn hình trước đó
            navController.navigate("admin")
        }
    }
    // Launcher để chọn hình ảnh từ thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it
                val inputStream = context.contentResolver.openInputStream(it)
                currentImageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99)),
    ) {
        UpdateDetailProductTopBar(navController)

        Spacer(modifier = Modifier.height(16.dp))

        // TextFields for Product properties
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
            ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = oldPrice,
                onValueChange = { oldPrice = it },
                label = { Text("Old Price", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Black,
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        currentImageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Current Product Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .align(CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Button để mở trình chọn hình ảnh từ thiết bị
            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120))
            ) {
                Text("Chọn hình ảnh mới")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Update Button
            Button(
                modifier = Modifier
                    .width(200.dp),
                onClick = {
                    showDialog = true // Hiển thị hộp thoại xác nhận khi nhấn nút "Update"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120))
            ) {
                Text("Update")
            }
        }

        // Hiển thị AlertDialog khi showDialog = true
        if (showDialog) {
            AlertDialog(
                containerColor = Color(181, 136, 99),
                onDismissRequest = { showDialog = false },
                title = { Text("Xác nhận cập nhật") },
                text = { Text("Bạn có chắc chắn muốn cập nhật thông tin sản phẩm này?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Cập nhật sản phẩm trong Firestore
                            product?.let {

                                val updatedImageBase64 = imageUri?.let { uri ->
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val bitmap = BitmapFactory.decodeStream(inputStream)
                                    bitmap?.let { firestoreHelper.bitmapToBase64(it) }
                                } ?: it.image

                                firestoreHelper.updateProductById(
                                    productId = it.productId,
                                    updatedProduct = Product(
                                        productId = it.productId,
                                        name = name.text,
                                        price = price.text.toDoubleOrNull() ?: 0.0,
                                        oldPrice = oldPrice.text.toDoubleOrNull(),
                                        image = updatedImageBase64.toString()
                                    )
                                )
                            }
                            // Đóng hộp thoại và quay lại màn hình trước đó
                            showDialog = false
                            navController.navigate("admin")
                        }
                    ) {
                        Text("Chấp nhận",color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("Hủy bỏ",color = Color.White)
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
