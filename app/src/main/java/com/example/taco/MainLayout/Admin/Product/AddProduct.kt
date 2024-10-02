package com.example.taco.MainLayout.Admin.Product

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, context: Context) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var oldPrice by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scrollState = rememberScrollState()
    val firestoreHelper = remember { FirestoreHelper() }

    // Launchers for image picker
    val openImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = uri
            val inputStream = context.contentResolver.openInputStream(uri)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
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
            modifier = Modifier
                .fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(8.dp))

            // Image picker button
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

            Button(
                onClick = {
                    val priceValue = price.toDoubleOrNull()
                    val oldPriceValue = oldPrice.toDoubleOrNull()
                    val imageBase64 = imageBitmap?.let { firestoreHelper.bitmapToBase64(it) }  // Mã hóa hình ảnh thành base64

                    if (priceValue != null) {
                        val product = Product(
                            name = name,
                            price = priceValue,
                            oldPrice = oldPriceValue,
                            image = imageBase64
                        )
                        firestoreHelper.addProduct(product)
                        // Navigate back or show a success message
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120))
            ) {
                Text("Add Product")
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
