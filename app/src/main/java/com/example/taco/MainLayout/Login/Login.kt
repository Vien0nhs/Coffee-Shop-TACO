package com.example.taco.MainLayout.Login

import CustomerDatabase
import Customers
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Customer
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.DataRepository.Firestore.FirebaseAPI.OrderProduct

import com.example.taco.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(navController: NavController) {
    val firestoreHelper = FirestoreHelper() // Khởi tạo FirestoreHelper
    val coroutineScope = rememberCoroutineScope() // Tạo coroutine scope
    var username by remember { mutableStateOf("") } // Trạng thái lưu trữ username
    var password by remember { mutableStateOf("") } // Trạng thái lưu trữ password
    var errorMessage by remember { mutableStateOf("") } // Trạng thái lưu trữ lỗi
    var passwordVisible by remember { mutableStateOf(false) } // Trạng thái hiển thị mật khẩu
    var customers by remember { mutableStateOf<List<Customers>>(emptyList()) } // Danh sách khách hàng sqlite
    val customersF = remember { mutableStateListOf<Customer>() } // Danh sách khách hàng firebase
    val otpCheck = remember { mutableStateOf(false) } // Trạng thái kiểm tra OTP
    val scrollState = rememberScrollState() // Trạng thái cuộn
    val showDialog = remember { mutableStateOf(false) } // Trạng thái hiển thị dialog
    val showCustomerDialog = remember { mutableStateOf(false)} // Trạng thái hiển thị dialog nhập thông tin khách hàng nếu là khách hàng mới
    var customerName by remember { mutableStateOf("") } // Thông tin khách hàng
    var customerPhone by remember { mutableStateOf("") } // Thông tin khách hàng
    val sql = CustomerDatabase(LocalContext.current) // Khởi tạo SQLite
    var verificationCode by remember { mutableStateOf("") } // Mã OTP
    val allCus = sql.getAllCustomers() // Danh sách khách hàng sqlite
    val context = LocalContext.current // Context hiện tại
    var showWelComAdminDialog by remember { mutableStateOf(false) } // Trạng thái hiển thị dialog chào mừng admin
    // Nếu danh sách khách hàng sqlite rỗng thì hiển thị dialog nhập thông tin khách hàng
    if(allCus.isEmpty()){
        showCustomerDialog.value = true
    }
    else{
        showCustomerDialog.value = false
    }

    /* Tải danh sách Account từ Firestore  */
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            customersF.addAll(firestoreHelper.getAllCustomers())
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
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Số điện thoại", color = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Black,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Bật bàn phím số
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Bỏ qua nếu bạn là khách cũ nhé !", color = Color.White, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Black,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Kiểm tra otp khách hàng
//                    if (otpCheck.value) {
//                        Button(
//                            onClick = {
//                                sendOtpToPhoneNumber(context, "+84${customerPhone.trimStart('0')}")
//                            },
//                            modifier = Modifier.padding(top = 16.dp)
//                        ) {
//                            Text("Gửi OTP")
//                        }
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        OutlinedTextField(
//                            value = verificationCode,
//                            onValueChange = { verificationCode = it },
//                            label = { Text("Mã OTP") },
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Button(
//                            onClick = {
//                                verifyOtpCode(context, verificationCode, onSuccess = {
//                                    otpCheck.value = false // Xác thực thành công
//                                    Toast.makeText(context, "Xác nhận OTP thành công", Toast.LENGTH_SHORT).show()
//                                }, onFailure = {
//                                    Toast.makeText(context, "Xác nhận OTP thất bại", Toast.LENGTH_SHORT).show()
//                                })
//                            },
//                            modifier = Modifier.padding(top = 16.dp)
//                        ) {
//                            Text("Xác nhận OTP")
//                        }
//                    }


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
                            text = if(errorMessage.isEmpty()){
                                "Quán sẽ không yêu cầu OTP để xác minh khách hàng " +
                                        " Vui lòng cung cấp tên và số điện thoại thực để" +
                                        " quán liên hệ với bạn khi có vấn đề về đơn hàng nhé! "
                            }
                            else{
                                errorMessage
                            },
                            color = if(errorMessage.isNotEmpty()) Color.Green else Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Kiểm tra họ tên và số điện thoại
                        if (validateCustomerInput(customerPhone)) {
                            coroutineScope.launch {
                                saveCustomerToFirebaseAndSQLite(
                                    customerPhone,
                                    firestoreHelper,
                                    sql,
                                    onExistingCustomer = {
                                        coroutineScope.launch {

                                            val cusname = firestoreHelper.getAllCustomers().find { it.phoneNumber == customerPhone }
                                            if(cusname != null){
                                                errorMessage = "Chào mừng quay lại ${cusname.cusName}! Đang đưa bạn về home..."
                                                delay(3000L)
                                                sql.insertCustomer(cusname.cusName, customerPhone) // Lưu lại thông tin khách hàng cũ], customerPhone)
                                            }
                                            navController.navigate("home") // Quay về home
                                            showCustomerDialog.value = false // Đóng dialog

                                        }
                                    },
                                    notExistingCustomer = {
                                        coroutineScope.launch {
//                                            otpCheck.value = true
//                                            if(otpCheck.value == false){
//
//                                            }
                                            if(customerName.isEmpty()){
                                                errorMessage = "Bạn là khách hàng mới, vui lòng cung cấp tên nhé !"
                                            }
                                            else{
                                                errorMessage = "Chào mừng khách hàng mới $customerName! Đang đưa bạn về home..."
                                                delay(3000L)
                                                showCustomerDialog.value = false // Đóng dialog
                                                // Nếu không tồn tại, lưu vào Firebase và SQLite
                                                val newCustomer = Customer(cusName = customerName, phoneNumber = customerPhone)
                                                firestoreHelper.addCustomer(newCustomer) // Lưu vào Firebase
                                                sql.insertCustomer(customerName = customerName, customerNumPhone = customerPhone) // Lưu vào SQLite
                                                navController.navigate("home") // Quay về home
                                            }
                                        }
                                    }
                                )
                            }
                            showCustomerDialog.value = false // Đóng dialog
                        }
                        else {
                            errorMessage = "Vui lòng nhập Số điện thoại của bạn."

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
            .background(Color(181, 136, 99))
    ) {
        LoginTopBar(navController)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(allCus.isNotEmpty()){
                val name = allCus[0].customerName
                val phone = allCus[0].customerNumPhone
                Text(
//                    modifier = Modifier
//                        .padding(vertical = 30.dp),
                    text = "Chào mừng! Khách $name",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
//                    modifier = Modifier
//                        .padding(vertical = 30.dp),
                    text = "Số của bạn: $phone",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.padding(bottom = 16.dp))

            HorizontalDivider(
                color = Color.White,
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
                val focusManager = LocalFocusManager.current

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
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next // Thiết lập ImeAction cho trường Username là Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) } // Chuyển focus đến trường Password
                    )
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
                        .padding(bottom = 32.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done // Thiết lập ImeAction cho trường Password là Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Xử lý khi nhấn nút "Done" trên bàn phím, ví dụ: đăng nhập
                            focusManager.clearFocus()
                        }
                    )
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                if(showWelComAdminDialog){
                    AlertDialog(
                        onDismissRequest = { showWelComAdminDialog = false },
                        title = { Text(text = "Chào mừng Admin của quán !", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = { Text(text = "Chúc bạn một ngày làm việc tốt !", color = Color.White) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showWelComAdminDialog = false
                                    navController.navigate("admin")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(181, 136, 99))
                            ) {
                                Text("Tới chức năng Admin", color = Color.White)
                            }
                        },
                        containerColor = Color(181, 136, 99), // Màu nâu RGB
                        textContentColor = Color.White
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if(username == Admin.adminName && password == Admin.adminPass){
                            showWelComAdminDialog = true
                            Admin.adminMode = true
                        }
                        else{
                            errorMessage = "Sai tài khoản hoặc mật khẩu"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(190, 160, 120)
                    )
                ) {
                    Text(text = "Login for Admin Functions")
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("home")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(190, 160, 120) )
                ){
                    Text(text = "Bỏ qua")

                }

                HorizontalDivider(
                    color = Color.White,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(vertical = 30.dp)
                        .padding(start = 30.dp, end = 30.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.coffeelogo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(75.dp)
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
        title = { Text("Đăng nhập") },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("search") }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        },
        actions = {
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
fun validateCustomerInput(phone: String): Boolean {
    val phoneRegex = "^\\+?\\d{10,11}$".toRegex() // Số điện thoại phải là 10-11 chữ số
    return phone.matches(phoneRegex)
}

// Hàm lưu thông tin khách hàng vào Firebase và SQLite
suspend fun saveCustomerToFirebaseAndSQLite(phone: String,firestoreHelper: FirestoreHelper,customerDatabase: CustomerDatabase,onExistingCustomer: () -> Unit,notExistingCustomer: () -> Unit) {
    // Kiểm tra xem số điện thoại có tồn tại trong Firestore không
    val existingCustomers = firestoreHelper.getAllCustomers()
    if (existingCustomers.any { it.phoneNumber == phone }) {

        onExistingCustomer() // Nếu tồn tại, gọi callback

    } else {

        notExistingCustomer()

    }
}
object Admin{
    val adminName = "admin"
    val adminPass = "admin123"
    var adminMode = false
}
//// Lưu lại ID xác minh và token để sử dụng cho việc xác nhận OTP
//var storedVerificationId: String? = null
//var resendToken: PhoneAuthProvider.ForceResendingToken? = null
//val auth = FirebaseAuth.getInstance()
//
//fun sendOtpToPhoneNumber(context: Context, phoneNumber: String) {
//    if (context is Activity) {
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(phoneNumber)
//            .setTimeout(60L, TimeUnit.SECONDS)
//            .setActivity(context)
//            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                    Toast.makeText(context, "Xác minh tự động thành công!", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onVerificationFailed(e: FirebaseException) {
//                    Toast.makeText(context, "Gửi OTP thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                    storedVerificationId = verificationId
//                    resendToken = token
//                    Toast.makeText(context, "Mã OTP đã được gửi!", Toast.LENGTH_SHORT).show()
//                }
//            })
//            .build()
//
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    } else {
//        Toast.makeText(context, "Lỗi: Context không phải là Activity", Toast.LENGTH_SHORT).show()
//    }
//}
//
//fun verifyOtpCode(
//    context: Context,
//    otpCode: String,
//    onSuccess: () -> Unit,
//    onFailure: () -> Unit
//) {
//    if (storedVerificationId != null) {
//        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otpCode)
//        FirebaseAuth.getInstance().signInWithCredential(credential)
//            .addOnCompleteListener(context as Activity) { task ->
//                if (task.isSuccessful) {
//                    onSuccess()
//                } else {
//                    onFailure()
//                }
//            }
//    } else {
//        Toast.makeText(context, "Mã xác minh không khả dụng", Toast.LENGTH_SHORT).show()
//    }
//}
