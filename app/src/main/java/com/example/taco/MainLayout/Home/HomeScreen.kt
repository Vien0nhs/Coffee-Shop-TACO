package com.example.taco.MainLayout.Home


import CustomerDatabase
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.Image.ImageList
import com.example.taco.DataRepository.Firestore.FirebaseAPI.Account
import com.example.taco.DataRepository.Firestore.FirebaseAPI.FirestoreHelper
import com.example.taco.MainLayout.Admin.AdminScreen
import com.example.taco.MainLayout.Admin.AdminTopBar

import com.example.taco.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.tan

@Composable
fun HomeScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DrawerMenu(navController, drawerState, scope, context)
    }
}

@Composable
fun DrawerMenu(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,

) {
    ModalNavigationDrawer(

        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            DrawerContent(navController, drawerState, scope)
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ){
                TopBotAppBar(navController, drawerState, scope, context)
            }
        }
    )
}

@Composable
fun DrawerContent(navController: NavController,drawerState: DrawerState, scope: CoroutineScope) {
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(1.0f)
            .background(Color(181, 136, 99))
    ){
        Column {
            IconButton(
                onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                modifier = Modifier
                    .padding(top = 55.dp, start = 7.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Close Menu", tint = Color.White)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(181, 136, 99)),
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = painterResource(id = R.drawable.coffeelogo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "TACO Coffee",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    color = Color.White
                    )
            }
            HorizontalDivider(
                color = Color.Black, // Màu của đường kẻ
                thickness = 1.dp, // Độ dày của đường kẻ
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(start = 16.dp, end = 16.dp)// Khoảng cách giữa các phần tử và đường kẻ
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        navController.navigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(181,136,99)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start // Căn chỉnh nội dung từ trái sang phải
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        val sql = CustomerDatabase(LocalContext.current)
                        val customerNames = sql.getAllCustomers()
                        val cusName = customerNames[0].customerName
                        val cusPhone = customerNames[0].customerNumPhone


                        if(cusName.isNotEmpty()){
                            Text(
                                text = "Hello ${cusName}!. Số điện thoại của bạn: $cusPhone ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 15.sp
                            )
                        }
                        else{
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
            //space
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showDialog.value = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(181, 136, 99)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ExitToApp, // Use the exit icon
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thoát người dùng.", // Change the text to "Exit"
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 15.sp
                        )
                    }
                    val sqlite = CustomerDatabase(LocalContext.current)

                    if (showDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showDialog.value = false },
                            title = { Text("Xác nhận thoát") },
                            text = { Text("Bạn có chắc chắn thoát người dùng ?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        sqlite.deleteAllCustomers()
                                        (context as? Activity)?.finishAffinity()
                                    }
                                ) {
                                    Text("Chấp nhận")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showDialog.value = false
                                    }
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
}

@Composable
fun TopBotAppBar(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,

) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(

        topBar = {
            when(selectedTab){
                0 -> {
                    HomeTopBar(navController, drawerState, scope)
                }
                1 -> SearchTopBar()
                3 -> PaymentsTopBar()
                4 -> AdminTopBar(navController)
            }
        },

        bottomBar = {
            BottomNavigationBar(navController, selectedTab, onTabSelected = { index -> selectedTab = index})
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)) {
            when (selectedTab) {
                1 -> SearchScreen(navController, context)
                2 -> CartScreen(navController)
                3 -> PaymentsScreen()
                4 -> AdminScreen(navController, context)
            }
        }
        if(selectedTab == 0){
            CategoryScreen(navController)
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(181, 136, 99),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home", color = Color.White) },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Search", color = Color.White) },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "OrderConfirm") },
            label = { Text("Cart", color = Color.White) },
            selected = selectedTab == 2,
            onClick = { navController.navigate("cart") },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Payment, contentDescription = "Cart") },
            label = { Text("Payments", color = Color.White) },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
        if(true){ //isAdminCheck.value
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = "Admin") },
                label = { Text("Admin", color = Color.White) },
                selected = selectedTab == 4,
                onClick = { navController.navigate("admin") },
                enabled = true
            )
        }
    }
}

@Composable
fun ImageRow() {
    val images = ImageList().imageResId

    LazyRow {
        items(images.size) { index ->
            Image(
                painter = painterResource(id = images[index]),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(200.dp)
            )
        }
    }

}

@Composable
fun CategoryScreen(navController: NavController) {
    val firestoreHelper = remember { FirestoreHelper() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
            .padding(top = 150.dp)
            .verticalScroll(scrollState)

    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Text(
                    text = "TACO Coffee Shop chúc bạn một ngày tốt lành! ",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 10.dp, top = 50.dp),
                    color = Color.White,
                    fontSize = 15.sp
                )
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Heart",
                    tint = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp, top = 50.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = Color.White, // Màu của đường kẻ
                thickness = 1.dp, // Độ dày của đường kẻ
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp)// Khoảng cách giữa các phần tử và đường kẻ
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            ) {

                CategoryButton(
                    imageResId = R.drawable.cake,
                    label = "Bánh",
                    onClick = { navController.navigate("cake") }
                )
                CategoryButton(
                    imageResId = R.drawable.coffee,
                    label = "Cà phê",
                    onClick = { navController.navigate("coffee") }
                )
                CategoryButton(
                    imageResId = R.drawable.juice,
                    label = "Nước ép",
                    onClick = { navController.navigate("juice") }
                )
                CategoryButton(
                    imageResId = R.drawable.milktea,
                    label = "Trà sữa",
                    onClick = { navController.navigate("milktea") }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                CategoryButton(
                    imageResId = R.drawable.otherdrinks,
                    label = "Món khác",
                    onClick = { navController.navigate("other") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = Color.White, // Màu của đường kẻ
                thickness = 1.dp, // Độ dày của đường kẻ
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp)// Khoảng cách giữa các phần tử và đường kẻ
            )
        }
    }
}

@Composable
fun CategoryButton(
    imageResId: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color(190, 160, 120), shape = CircleShape)
                .clickable(onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(45.dp)
                    .clip(CircleShape)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(navController: NavController, drawerState: DrawerState, scope: CoroutineScope) {

    Column {
        CenterAlignedTopAppBar(
            title = { Text("Home") },
            navigationIcon = {
                IconButton(
                    onClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu Icon", tint = Color.White)
                }
            },

            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(181, 136, 99),
                titleContentColor = Color.White
            )
        )
        HorizontalDivider(
            color = Color(190, 160, 120), // Màu của đường kẻ
            thickness = 3.dp, // Độ dày của đường kẻ
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.coffeebackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )

            ImageRow()

        }
        HorizontalDivider(
            color = Color(190, 160, 120), // Màu của đường kẻ
            thickness = 3.dp, // Độ dày của đường kẻ
        )
    }
}