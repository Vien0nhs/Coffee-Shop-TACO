package com.example.taco.DataRepository

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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.example.taco.FirebaseAPI.FirestoreHelper
import com.example.taco.FirebaseAPI.Product

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
            .verticalScroll(scrollState)
    ) {
        AddProductTopBar(navController)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Name:", fontSize = 16.sp)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Price:", fontSize = 16.sp)
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Old Price (optional):", fontSize = 16.sp)
        OutlinedTextField(
            value = oldPrice,
            onValueChange = { oldPrice = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Image picker button
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99)),
            onClick = { openImagePickerLauncher.launch("image/*") }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Selected Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val priceValue = price.toDoubleOrNull()
                val oldPriceValue = oldPrice.toDoubleOrNull()
                val imageBase64 = imageBitmap?.let { bitmapToBase64(it) }  // Mã hóa hình ảnh thành base64

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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
        ) {
            Text("Add Product")
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

// Chuyển đổi Bitmap thành chuỗi base64
fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Chất lượng nén 100%
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}
