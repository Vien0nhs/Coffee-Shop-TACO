package com.example.taco.MainLayout.Admin.Product

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product

@Composable
fun UpdateDetailProductScreen(navController: NavController, productId: String, context: Context) {
    val firestoreHelper = remember { FirestoreHelper() }
    var product by remember { mutableStateOf<Product?>(null) }

    // Khai báo các biến trạng thái cho các trường của sản phẩm
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var oldPrice by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Lấy thông tin sản phẩm từ Firestore
    LaunchedEffect(productId) {
        product = firestoreHelper.getProductById(productId)
        product?.let {
            name = TextFieldValue(it.name)
            price = TextFieldValue(it.price.toString())
            oldPrice = TextFieldValue(it.oldPrice?.toString() ?: "")
            it.image?.let { imageUrl ->
                firestoreHelper.loadImageFromStorage(imageUrl, context) { bitmap ->
                    currentImageBitmap = bitmap
                }
            }
        } ?: run {
            navController.navigate("admin")
        }
    }

    // Tải hình ảnh từ URI nếu có
    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            currentImageBitmap = firestoreHelper.loadImageFromUri(context, uri.toString())
        }
    }

    // Launcher để chọn hình ảnh từ thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                imageUri = it // Cập nhật imageUri
                val inputStream = context.contentResolver.openInputStream(it)
                currentImageBitmap = BitmapFactory.decodeStream(inputStream) // Cập nhật bitmap
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

        // Hiển thị hình ảnh hiện tại hoặc placeholder
        if (currentImageBitmap != null) {
            Image(
                bitmap = currentImageBitmap!!.asImageBitmap(),
                contentDescription = "Current Product Image",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        } else {
            // Hiển thị một placeholder nếu không có hình ảnh
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.White)
            }
        }

        // Button để mở trình chọn hình ảnh
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120)),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Chọn hình ảnh mới")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button để cập nhật sản phẩm
        Button(
            modifier = Modifier
                .width(200.dp)
                .align(Alignment.CenterHorizontally),
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120))
        ) {
            Text("Update")
        }

        // Hiển thị AlertDialog khi showDialog = true
        // Confirm Update Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Xác nhận cập nhật") },
                text = { Text("Bạn có chắc chắn muốn cập nhật thông tin sản phẩm này?") },
                confirmButton = {
                    TextButton(onClick = {
                        product?.let { currentProduct ->
                            currentProduct.name = name.text
                            currentProduct.price = price.text.toDoubleOrNull() ?: 0.0
                            currentProduct.oldPrice = oldPrice.text.toDoubleOrNull()

                            firestoreHelper.updateProductById(
                                productId = currentProduct.productId,
                                updatedProduct = currentProduct,
                                newImageUri = imageUri?.toString()
                            )
                        }

                        showDialog = false
                        navController.navigate("admin")
                    }) {
                        Text("Chấp nhận", color = Color.White)
                    }
                }
,
                        dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Hủy bỏ", color = Color.White)
                    }
                }
            )
        }


    }
}

private fun updateProduct(
    productId: String,
    name: String,
    price: Double,
    oldPrice: Double?,
    imageUrl: String?
) {
    // Cập nhật sản phẩm với FirestoreHelper
    FirestoreHelper().updateProductById(
        productId,
        Product(productId = productId, name = name, price = price, oldPrice = oldPrice, image = imageUrl.toString()),
        imageUrl
    )
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
