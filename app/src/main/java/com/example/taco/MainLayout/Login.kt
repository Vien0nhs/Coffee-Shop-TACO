    package com.example.taco.MainLayout

    import androidx.compose.foundation.*
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.Modifier
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Visibility
    import androidx.compose.material.icons.filled.VisibilityOff

    import androidx.compose.ui.Alignment
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.example.taco.MainLayout.GlobalLoginVariables.AdminName
    import com.example.taco.MainLayout.GlobalLoginVariables.isAdmin
    import com.example.taco.MainLayout.GlobalLoginVariables.userName
    import com.example.taco.MainLayout.GlobalLoginVariables.passWord
    import com.example.taco.R

    object GlobalLoginVariables{
        var isAdmin: Boolean = false
        var AdminName: String = ""
        var userName: String = ""
        var passWord: String = ""
    }
    @Composable
    fun LoginScreen(navController: NavController){
        var username by remember {
            mutableStateOf(userName)
        }
        var password by remember {
            mutableStateOf(passWord)
        }
        var errorMessage by remember {
            mutableStateOf("")
        }
        var passwordVisible by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        var showDialog = remember { mutableStateOf(false) }
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
                    color = Color.Black, // Màu của đường kẻ
                    thickness = 1.dp, // Độ dày của đường kẻ
                    modifier = Modifier
                        .padding(vertical = 1.dp)
                        .padding(start = 30.dp, end = 30.dp)// Khoảng cách giữa các phần tử và đường kẻ
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if(isAdmin == true){
                        Text(
                            text = "You're in Admin Mode",
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 30.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                    else{
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }


                    // TextField cho Username
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // TextField cho Password
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation =
                        if(passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },// Ẩn mật khẩu
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    )

                    // Nút "Đăng nhập"
                    Button(
                        modifier = Modifier.fillMaxWidth(),

                        onClick = {
                            if(username == "AdminVien" && password == "Admin123"){
                                isAdmin = true
                                AdminName = username
                                navController.navigate("home")
                            } else{
                                errorMessage = "Invalid credentials."
                            }
                        },
                        enabled = !isAdmin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(181,136,99) )// Màu nền của Button

                    ) {
                        Text(text = "Login for Admin CRUD Feature")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showDialog.value = true
                        },
                        enabled = isAdmin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(181,136,99) )
                    ){
                        Text(text = "Logout")

                    }
                    if(errorMessage.isNotEmpty()){
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    HorizontalDivider(
                        color = Color.Black, // Màu của đường kẻ
                        thickness = 1.dp, // Độ dày của đường kẻ
                        modifier = Modifier
                            .padding(vertical = 30.dp)
                            .padding(start = 30.dp, end = 30.dp)// Khoảng cách giữa các phần tử và đường kẻ
                    )
                    Text(
                        text = "Teacher Binh is a great teacher although I don't get at all in he class. But he is very enthusiastic for he's student. I'm very grateful by to be a student of him.",
                        color = Color.White,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(top = 16.dp)
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
                            text = { Text("Bạn có chắc chắn đăng xuất ${AdminName} ?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        isAdmin = false
                                        AdminName = ""
                                        navController.navigate("home")
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginTopBar(navController: NavController) {
        CenterAlignedTopAppBar(
            title = { Text("Login Part") },
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