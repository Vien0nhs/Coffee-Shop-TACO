package com.example.taco.MainLayout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
//import com.example.taco.Data.DatabaseTACO
import com.example.taco.FirebaseAPI.FirestoreHelper
import com.example.taco.FirebaseAPI.Product
import android.util.Base64
import androidx.compose.ui.text.TextStyle
import com.example.taco.FirebaseAPI.base64ToBitmap

@Composable
fun AdminScreen(navController: NavController, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99)),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        AdminTopBar(navController)

        Button(
            onClick = { navController.navigate("add") },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(190, 160, 120))
        ) {
            Text("Add a Product")
        }

        // New section for managing products (update and delete)
        ManageProductsScreen(navController, context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Admin") },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("home") }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(navController: NavController, context: Context) {
    // Create an instance of FirestoreHelper
    val firestoreHelper = remember { FirestoreHelper() }
    var isLoading by remember {
        mutableStateOf(false)
    }
    // State to hold the list of products
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch products from Firestore
    LaunchedEffect(searchQuery) {
        isLoading = true
        products = firestoreHelper.getAllProducts().filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .background(Color(181, 136, 99))
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        Row(
            modifier = Modifier
                .background(Color(181, 136, 99))
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                },
                textStyle = TextStyle(
                    color = Color.White
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
                label = { Text("Tìm kiếm sản phẩm",
                    color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            // Show loading indicator while fetching data
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            LazyColumn {
                items(products) { product ->
                    HorizontalDivider(
                        color = Color.White,
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 16.dp, end = 16.dp)
                    )
                    ProductRow(
                        product = product,
                        onUpdate = { productId ->
                            navController.navigate("updateDetail/$productId")
                        },
                        onDelete = { productId ->
                            firestoreHelper.deleteProductById(productId)
                            // Refresh product list after deletion
                            LaunchedEffect(Unit) {
                                products = firestoreHelper.getAllProducts().filter {
                                    it.name.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        },
                        navController
                    )
                }
            }
        }
    }
}



@Composable
fun ProductRow(
    product: Product,
    onUpdate: (String) -> Unit,
    onDelete: @Composable (String) -> Unit,
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    val imageBitmap = product.image?.let { base64ToBitmap(it)?.asImageBitmap() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageBitmap?.let {
                Image(bitmap = it, contentDescription = "Product Image",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop, // Đảm bảo hình ảnh không bị biến dạng
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Price: ${String.format("%.3f", product.price)} VND",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp
                )
                product.oldPrice?.let {
                    Text(
                        text = "Old Price: ${String.format("%.3f", it)} VND",
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 13.sp
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Update button
            IconButton(
                modifier = Modifier
                    .size(20.dp),
                onClick = {
                    onUpdate(product.productId)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Update Product",
                    tint = Color.Blue
                )
            }
            Text(
                text = "Edit",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            // Delete button
            IconButton(
                modifier = Modifier
                    .size(20.dp),
                onClick = { showDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Product",
                    tint = Color.Red
                )
            }
            Text(
                text = "Delete",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Xác nhận xoá") },
            text = { Text("Bạn có chắc chắn xoá sản phẩm này khỏi hệ thống?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Call the Firestore delete method
                        val firestoreHelper = FirestoreHelper() // Initialize FirestoreHelper
                        firestoreHelper.deleteProductById(product.productId)
                        showDialog = false
                        // Optionally, trigger a refresh of the product list
                        navController.navigate("admin")
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Huỷ")
                }
            }
        )
    }
}

