package com.example.taco.MainLayout.Admin.Product

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Product
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, context: Context) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var oldPrice by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val firestoreHelper = remember { FirestoreHelper() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val openImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = uri
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                imageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
            .verticalScroll(scrollState)
    ) {
        AddProductTopBar(navController)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
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
                value = oldPrice,
                onValueChange = { oldPrice = it },
                label = { Text("Old Price", color = Color.White) },
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

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120)),
                onClick = { openImagePickerLauncher.launch("image/*") }) {
                Text("Select Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            imageBitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "Selected Image")
            }

            Spacer(modifier = Modifier.height(50.dp))

            var showDialog by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    if (imageBitmap == null) {
                        showDialog = true  // Hiển thị dialog nếu không có ảnh
                    } else {
                        val priceValue = price.toDoubleOrNull()
                        val oldPriceValue = oldPrice.toDoubleOrNull()

                        if (priceValue == null || oldPriceValue == null) {
                            Toast.makeText(context, "Invalid price or old price value", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Gọi hàm uploadImageToFirebase
                        firestoreHelper.uploadImageToFirebase(
                            context = context,
                            imageBitmap = imageBitmap!!,
                            onSuccess = { imageUrl ->
                                // Sau khi tải ảnh thành công, tạo đối tượng Product
                                val product = Product(
                                    name = name,
                                    price = priceValue,
                                    oldPrice = oldPriceValue,
                                    image = imageUrl
                                )

                                // Lưu product vào Firestore
                                firestoreHelper.addProduct(
                                    product = product,
                                    onSuccess = {
                                        Toast.makeText(context, "Product added successfully!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    },
                                    onFailure = { exception ->
                                        Toast.makeText(context, "Failed to add product: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("Firestore", "Error adding product", exception)
                                    }
                                )

                            },
                            onFailure = { exception ->
                                Toast.makeText(context, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120))
            ) {
                Text("Add Product")
            }

// Hiển thị dialog khi không có ảnh
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("No Image Selected") },
                    text = { Text("Please select an image before adding the product.") },
                    confirmButton = {
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Add Product") },
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
