package com.example.taco.MainLayout.Login

import CustomerDatabase
import Customers
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Account
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Customer
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.OrderProduct

import com.example.taco.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val firestoreHelper = FirestoreHelper()
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var allAccounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var customers by remember { mutableStateOf<List<Customers>>(emptyList()) }
    val customersF = remember { mutableStateListOf<Customer>() }

    val scrollState = rememberScrollState()
    val showDialog = remember { mutableStateOf(false) }
    val showCustomerDialog = remember { mutableStateOf(false)}
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    val sql = CustomerDatabase(LocalContext.current)
    val allCus = sql.getAllCustomers()
    if(allCus.isEmpty()){
        showCustomerDialog.value = true
    }
    else{
        showCustomerDialog.value = false
    }

    /* Tải danh sách Account từ Firestore  */
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                allAccounts = firestoreHelper.getAllAccounts()
                customersF.addAll(firestoreHelper.getAllCustomers())
                println("Fetched accounts: $allAccounts") // Debugging line
            } catch (e: Exception) {
                println("Error loading accounts: ${e.message}") // Debugging line
            }
        }
    }

    /* Hiển thị dialog Nhập thông tin khách hành nếu chưa có trong SQLite và
     thêm khách hàng vào cả sqlite và firebase */
    if (showCustomerDialog.value) {
        AlertDialog(
            containerColor = Color(181, 136, 99),
            onDismissRequest = { /* không cho phép đóng khi chưa nhập đúng */ },
            title = { Text("Nhập thông tin khách hàng") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Họ tên", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Black,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Số điện thoại", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Black,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Bật bàn phím số
                        modifier = Modifier.fillMaxWidth()
                    )
                    if(errorMessage.isNotEmpty()){
                        Text(errorMessage,
                            color = Color.Green,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    else{
                        Text(
                            text = "!Vui lòng nhập đúng định dạng số điện thoại Việt Nam và !tên khách " +
                                    "hàng là tên có dấu của chủ tài khoản ngân hàng thanh toán ở app.",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                }
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        // Kiểm tra họ tên và số điện thoại
                        if (validateCustomerInput(customerName, customerPhone)) {
                            coroutineScope.launch {
                                saveCustomerToFirebaseAndSQLite(
                                    customerName,
                                    customerPhone,
                                    firestoreHelper,
                                    sql,
                                    onExistingCustomer = {
                                        errorMessage = "Số điện thoại đã tồn tại trong hệ thống. Đang quay trở về trang chủ sau 2 giây..."

                                        coroutineScope.launch {
                                            delay(2000L)
                                            // Thông báo rằng số điện thoại đã tồn tại
                                            sql.insertCustomer(customerName, customerPhone)
                                            showCustomerDialog.value = false // Đóng dialog
                                            navController.navigate("home") // Quay về home
                                        }
                                    }
                                )
                            }
                            showCustomerDialog.value = false // Đóng dialog
                        } else {
                            errorMessage = "Thông tin không hợp lệ!"
                        }

                    }
                ) {
                    Text("Lưu", color = Color.White)
                }
            }
        )
    }

    // Hiển thị layout đăng nhập
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 115, 70))
            .verticalScroll(scrollState),
    ) {
        LoginTopBar(navController)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.coffeelogo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
            )
            Text(
                modifier = Modifier
                    .padding(vertical = 30.dp),
                text = "TACO Coffee",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 24.sp,
                color = Color.White
            )
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .padding(start = 30.dp, end = 30.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Black,
                ),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Black,
                    ),
                    visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(181, 136, 99)
                    )
                ) {
                    Text(text = "Login")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showDialog.value = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(181,136,99) )
                ){
                    Text(text = "Logout")

                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("home")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(181,136,99) )
                ){
                    Text(text = "Skip login part")

                }
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                HorizontalDivider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(vertical = 30.dp)
                        .padding(start = 30.dp, end = 30.dp)
                )
                Text(
                    text = "Teacher Binh is a great teacher although I don't get at all in he class. But he is very enthusiastic for he's student. I'm very grateful by to be a student of him.",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        title = { Text("Xác nhận đăng xuất") },
                        text = { Text("Bạn có chắc chắn đăng xuất  ?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDialog.value = false
                                }
                            ) {
                                Text("Chấp nhận")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog.value = false }
                            ) {
                                Text("Hủy bỏ")
                            }
                        }
                    )
                }
            }
        }
    }

}



// Hàm TopBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = { Text("Login Part") },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(
                    imageVector = Icons.Default.Home,
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

// Hàm kiểm tra định dạng số điện thoại
fun validateCustomerInput(name: String, phone: String): Boolean {
    val phoneRegex = "^\\+?\\d{10,11}$".toRegex() // Số điện thoại phải là 10-11 chữ số
    return name.isNotEmpty() && phone.matches(phoneRegex)
}

// Hàm lưu thông tin khách hàng vào Firebase và SQLite
suspend fun saveCustomerToFirebaseAndSQLite(
    name: String,
    phone: String,
    firestoreHelper: FirestoreHelper,
    customerDatabase: CustomerDatabase,
    onExistingCustomer: () -> Unit // Callback if customer already exists
) {
    // Kiểm tra xem số điện thoại có tồn tại trong Firestore không
    val existingCustomers = firestoreHelper.getAllCustomers()
    if (existingCustomers.any { it.phoneNumber == phone }) {
        onExistingCustomer() // Nếu tồn tại, gọi callback
    } else {
        // Nếu không tồn tại, lưu vào Firebase và SQLite
        val newCustomer = Customer(cusName = name, phoneNumber = phone)
        firestoreHelper.addCustomer(newCustomer) // Lưu vào Firebase
        customerDatabase.insertCustomer(customerName = name, customerNumPhone = phone) // Lưu vào SQLite
    }
}