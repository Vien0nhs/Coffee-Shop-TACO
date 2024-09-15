package com.example.taco.MainLayout

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.taco.DataRepository.DatabaseTACO
import com.example.taco.DataRepository.ImageList
import com.example.taco.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.taco.MainLayout.GlobalLoginVariables.AdminName

@Composable
fun HomeScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,

) {
    DrawerMenu(navController, drawerState, scope, context)
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
                Icon(Icons.Default.Menu, contentDescription = "Close Menu")
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
                        if(AdminName.isNotEmpty()){
                            Text(
                                text = "Hello ${AdminName}!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 15.sp
                            )
                        } else{
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
                            text = "Exit", // Change the text to "Exit"
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 15.sp
                        )
                    }
                    if (showDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showDialog.value = false },
                            title = { Text("Xác nhận thoát") },
                            text = { Text("Bạn có chắc chắn thoát ứng dụng ?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        (context as? Activity)?.finishAffinity()
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
}

@Composable
fun TopBotAppBar(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,

) {
    val dbhelper = DatabaseTACO(context)
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(

        topBar = {
            when(selectedTab){
                0 -> {
                    HomeTopBar(drawerState, scope)
                }
                1 -> SearchTopBar()
                2 -> CartTopBar()
                3 -> PaymentsTopBar()
                4 -> AdminTopBar(navController)
            }
        },

        bottomBar = {
            BottomNavigationBar(navController, selectedTab) { index ->
                selectedTab = index
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(drawerState: DrawerState, scope: CoroutineScope) {
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
                    Icon(Icons.Default.Menu, contentDescription = "Menu Icon")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(181, 136, 99),
                titleContentColor = Color.White
            )
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
            onClick = { onTabSelected(2) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Payment, contentDescription = "Cart") },
            label = { Text("Payments", color = Color.White) },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Admin") },
            label = { Text("Admin", color = Color.White) },
            selected = selectedTab == 4,
            onClick = { navController.navigate("admin") },
            enabled = true
        )
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
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(181, 136, 99))
            .padding(top = 150.dp)
    ) {

        Text(
            text = "Hoping you enjoys our service!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 10.dp, top = 70.dp),
            color = Color.White
        )
        Row {
            CategoryButton(
                imageResId = R.drawable.cake,
                label = "Cake",
                onClick = { navController.navigate("cake") }
            )
            CategoryButton(
                imageResId = R.drawable.coffee,
                label = "Coffee",
                onClick = { navController.navigate("coffee") }
            )
            CategoryButton(
                imageResId = R.drawable.juice,
                label = "Juices",
                onClick = { navController.navigate("juice") }
            )
            CategoryButton(
                imageResId = R.drawable.milktea,
                label = "Milk Tea",
                onClick = { navController.navigate("milktea") }
            )
        }
        Row {
            CategoryButton(
                imageResId = R.drawable.otherdrinks,
                label = "Other",
                onClick = { navController.navigate("other") }
            )
        }
        HorizontalDivider(
            color = Color.Black, // Màu của đường kẻ
            thickness = 1.dp, // Độ dày của đường kẻ
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp)// Khoảng cách giữa các phần tử và đường kẻ
        )
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
                .size(70.dp)
                .background(Color.LightGray, shape = CircleShape)
                .clickable(onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
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

