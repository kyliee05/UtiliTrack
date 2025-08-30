package com.example.reportactivity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.Manifest
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale


import coil.compose.rememberAsyncImagePainter

import com.example.reportactivity.ui.theme.ReportActivityTheme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import okhttp3.OkHttpClient
import okhttp3.Request

import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


import org.json.JSONObject

import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

import androidx.compose.ui.viewinterop.AndroidView



import androidx.compose.foundation.layout.fillMaxSize
import com.google.android.gms.location.LocationServices


import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import androidx.core.content.res.ResourcesCompat


import androidx.compose.material3.Text

import androidx.compose.material.icons.filled.Menu

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed

import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable

import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.reportactivity.network.RetrofitClient


import com.example.reportactivity.model.RegisterRequest
import com.example.reportactivity.model.LoginRequest
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color

import androidx.compose.material.icons.filled.CheckCircle









class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().isMapViewHardwareAccelerated = true

        setContent {
            ReportActivityTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(navController = navController)
                    }
                    composable("register") {
                        RegisterScreen(navController = navController)
                    }
                    composable("dashboards") {
                        UserDashboard()
                    }
                }

            }
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val apiService = RetrofitClient.instance

    // Register Button click
    fun handleRegister() {
        if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val registerRequest = RegisterRequest(name, email, password)
                    val response = apiService.register(registerRequest)
                    if (response.isSuccessful) {
                        // Handle successful registration
                        navController.navigate("login") // Navigate back to login screen
                    } else {
                        // Handle error in registration
                    }
                } catch (e: Exception) {
                    // Handle network or other errors
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,  // Center content vertically
        horizontalAlignment = Alignment.CenterHorizontally  // Center content horizontally
    ) {

        // Back Icon Button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start) // Align the icon to the start
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_revert), // Default back arrow icon
                contentDescription = "Back"
            )
        }

        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp) // Adjust the size as needed
        )

        Spacer(modifier = Modifier.height(32.dp))  // Space between logo and text fields

        // TextField for Name
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))  // Space between fields

        // TextField for Email
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))  // Space between fields

        // TextField for Password
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // Space before button

        // Register Button
        Button(
            onClick = { handleRegister() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Register")
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = RetrofitClient.instance

    fun handleLogin() {
        if (email.isNotBlank() && password.isNotBlank()) {
            isLoading = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val loginRequest = LoginRequest(email, password)
                    val response = apiService.login(loginRequest)
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse?.success == true) {
                                navController.navigate("dashboard")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Login failed: ${loginResponse?.message ?: "Unknown error"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    isLoading = false
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,  // Center content vertically
        horizontalAlignment = Alignment.CenterHorizontally  // Center content horizontally
    ) {
        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp) // Adjust the size as needed
        )

        Spacer(modifier = Modifier.height(32.dp))  // Space between logo and text fields

        // TextField for Email
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))  // Space between fields

        // TextField for Password
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))  // Space before button

        // Login Button
        Button(
            onClick = { handleLogin() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Login")
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        // Navigation to Register screen
        Text(
            text = "Don't have an account? Register here.",
            modifier = Modifier
                .clickable { navController.navigate("register") }
                .padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboard() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(-1) } // -1 shows the grid

    val items = listOf(
        "Report an Issue",
        "View Reports",
        "Issues on Map",
        "Announcements"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Navigation", Modifier.padding(16.dp))
                Divider()
                items.forEachIndexed { index, label ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedItem == -1) "User Dashboard" else items[selectedItem],
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectedItem == -1) {
                                scope.launch { drawerState.open() }
                            } else {
                                selectedItem = -1
                            }
                        }) {
                            Icon(
                                imageVector = if (selectedItem == -1) Icons.Filled.Menu else Icons.Filled.ArrowBack,
                                contentDescription = "Navigation"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedItem == -1) {
                    // Logo below "User Dashboard"
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(150.dp)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(items) { index, label ->
                            Card(
                                onClick = { selectedItem = index },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Example drawable icons
                                    val iconRes = when (index) {
                                        0 -> R.drawable.report
                                        1 -> R.drawable.viewreport
                                        2 -> R.drawable.map
                                        3 -> R.drawable.announcement
                                        else -> R.drawable.ic_default
                                    }

                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = label,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = label, style = MaterialTheme.typography.titleMedium)
                                }
                            }

                        }
                    }
                } else {
                    when (selectedItem) {
                        0 -> ReportForm()
                        1 -> ViewReportsScreen()
                        2 -> IssuesOnMapScreen()
                        3 -> AnnouncementsScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun ReportForm() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scrollState = rememberScrollState()

    var selectedCategory by remember { mutableStateOf("Road Issue") }
    var selectedSubcategory by remember { mutableStateOf("Potholes") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf("Low") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                } else {
                    Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val categoryToSubcategories = mapOf(
        "Road Issue" to listOf("Potholes and road surface damage", "Unmarked or faded pedestrian crossings and road signs"
            , "Traffic light malfunctions","Blocked or narrow sidewalks","Illegal parking and obstructions"),
        "Electricity" to listOf("Power Outage", "Exposed or damaged power lines", "Exposed or damaged power lines",
            "Unauthorized connections (illegal tapping)","Lack of streetlights or broken public lighting"),
        "Water Supply" to listOf("Low water pressure or water shortages", "Leaking pipelines or water mains",
            "Contaminated or discolored water","Unscheduled water service interruptions","Illegal water tapping or connections"),
        "Waste" to listOf("Uncollected household garbage", "Overflowing public trash bins",
            "Improper segregation and mixed waste disposal","Illegal dumping or open burning of waste","Poor drainage due to clogged garbage")
    )
    val priorities = listOf("Low", "Medium", "High")

    val categories = categoryToSubcategories.keys.toList()
    val subcategories = categoryToSubcategories[selectedCategory] ?: emptyList()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Submit a Report", style = MaterialTheme.typography.headlineSmall)

        // Category Dropdown
        Box {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryExpanded = !categoryExpanded },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Category Dropdown",
                        modifier = Modifier.clickable { categoryExpanded = !categoryExpanded }
                    )
                }
            )

            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            selectedSubcategory = categoryToSubcategories[category]?.firstOrNull() ?: ""
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Subcategory Dropdown
        Box {
            OutlinedTextField(
                value = selectedSubcategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Subcategory") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { subcategoryExpanded = !subcategoryExpanded },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Subcategory Dropdown",
                        modifier = Modifier.clickable { subcategoryExpanded = !subcategoryExpanded }
                    )
                }
            )

            DropdownMenu(
                expanded = subcategoryExpanded,
                onDismissRequest = { subcategoryExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                subcategories.forEach { sub ->
                    DropdownMenuItem(
                        text = { Text(sub) },
                        onClick = {
                            selectedSubcategory = sub
                            subcategoryExpanded = false
                        }
                    )
                }
            }
        }

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )
        // Priority Dropdown
        Box {
            OutlinedTextField(
                value = selectedPriority,
                onValueChange = {},
                readOnly = true,
                label = { Text("Priority Level") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { priorityExpanded = !priorityExpanded },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Priority Dropdown",
                        modifier = Modifier.clickable { priorityExpanded = !priorityExpanded }
                    )
                }
            )

            DropdownMenu(
                expanded = priorityExpanded,
                onDismissRequest = { priorityExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                priorities.forEach { priority ->
                    DropdownMenuItem(
                        text = { Text(priority) },
                        onClick = {
                            selectedPriority = priority
                            priorityExpanded = false
                        }
                    )
                }
            }
        }


        // Photo Picker
        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Upload Photo")
        }

        // Image Preview
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // Get Location Button with permission check
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        } else {
                            Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(locationPermission)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Use My Location")
        }

        // Show current location if available
        if (latitude != null && longitude != null) {
            Text("Latitude: $latitude")
            Text("Longitude: $longitude")
        }

        // Submit Button
        Button(
            onClick = {
                if (latitude != null && longitude != null) {
                    submitReportWithPhoto(
                        context = context,
                        userId = "123",
                        category = selectedCategory,
                        subcategory = selectedSubcategory,
                        description = description,
                        latitude = latitude!!,
                        longitude = longitude!!,
                        photoUri = imageUri,
                        priority = selectedPriority
                    )
                } else {
                    Toast.makeText(context, "Please provide location before submitting", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Report")
        }
    }
}


private fun submitReport(
    context: android.content.Context,
    userId: String,
    category: String,
    subcategory: String,
    description: String,
    latitude: Double,
    longitude: Double,
    photoUrl: String = ""
) {
    val json = JSONObject().apply {
        put("user_id", userId)
        put("category", category)
        put("subcategory", subcategory)
        put("description", description)
        put("latitude", latitude)
        put("longitude", longitude)
        put("photo_url", photoUrl)
    }

    val client = OkHttpClient()
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("http://192.168.1.5:8080/Capstone_Proj/add_report.php") // Replace with your local IP or public domain
        .post(body)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            println("Server Response: $responseBody") // Log the entire response to debug

            if (response.isSuccessful) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_LONG).show()
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Submission failed: Response - $responseBody", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
fun submitReportWithPhoto(
    context: Context,
    userId: String,
    category: String,
    subcategory: String,
    description: String,
    latitude: Double,
    longitude: Double,
    photoUri: Uri?,
    priority: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val contentResolver = context.contentResolver
            val client = OkHttpClient()

            val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart("category", category)
                .addFormDataPart("subcategory", subcategory)
                .addFormDataPart("description", description)
                .addFormDataPart("latitude", latitude.toString())
                .addFormDataPart("longitude", longitude.toString())
                .addFormDataPart("priority", priority)

            // Add image if exists
            photoUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                val fileName = "photo_${System.currentTimeMillis()}.jpg"

                bytes?.let {
                    val imageBody = it.toRequestBody("image/*".toMediaTypeOrNull())
                    requestBodyBuilder.addFormDataPart("photo", fileName, imageBody)
                }
            }

            val requestBody = requestBodyBuilder.build()

            val request = Request.Builder()
                .url("http://192.168.1.5:8080/Capstone_Proj/add_report.php") // change this
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to submit report", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// **View Reports** - Fetch and display reports from backend
@Composable
fun ViewReportsScreen() {
    val context = LocalContext.current
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchReports(context) { fetchedReports ->
            reports = fetchedReports
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            reports.isEmpty() -> {
                Text("No reports found.")
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(reports) { report ->
                        ReportItem(report = report) // Pass each report to ReportItem
                    }
                }
            }
        }
    }
}


fun fetchReports(context: Context, onSuccess: (List<Report>) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://192.168.1.5:8080/Capstone_Proj/get_reports.php")
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val jsonArray = jsonObject.getJSONArray("reports")
                val reports = mutableListOf<Report>()

                for (i in 0 until jsonArray.length()) {
                    val jsonReport = jsonArray.getJSONObject(i)

                    val id = jsonReport.optInt("report_id", 0)
                    val category = jsonReport.optString("category", "Unknown")
                    val description = jsonReport.optString("description", "No description")
                    val latitude = jsonReport.optDouble("latitude", 0.0)
                    val longitude = jsonReport.optDouble("longitude", 0.0)
                    val photoUrl = jsonReport.optString("photo_url", null)
                    val verified = jsonReport.optString("is_verified", "0") == "1"
                    val status = jsonReport.optString("status", "Pending") // Fallback to Pending



                    reports.add(
                        Report(
                            id = id,
                            category = category,
                            description = description,
                            latitude = latitude,
                            longitude = longitude,
                            photoUrl = photoUrl,
                            verified = verified,
                            status = status
                        )
                    )
                }


                withContext(Dispatchers.Main) {
                    onSuccess(reports)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}



// Report item layout for displaying each report

@Composable
fun ReportItem(report: Report) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category: ${report.category}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (report.verified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Text(
                text = "Description: ${report.description}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Status: ${report.status}",
                style = MaterialTheme.typography.bodySmall,
                color = when (report.status.lowercase()) {
                    "resolved" -> Color(0xFF4CAF50)
                    "in progress" -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
            )

            report.photoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter("http://192.168.1.5:8080/Capstone_Proj/uploads/$url"),
                    contentDescription = "Report Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

data class Report(
    val id: Int,
    val category: String,
    val description: String,
    val photoUrl: String? = null,
    val latitude: Double,
    val longitude: Double,
    val verified: Boolean, // <- Add this field
    val status: String // <-- NEW
)


@Composable
fun IssuesOnMapScreen() {
    val context = LocalContext.current
    val reports = remember { mutableStateListOf<Report>() }
    val markersUpdated = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchReports(context) { fetched ->
            reports.clear()
            reports.addAll(fetched)
            markersUpdated.value = true
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(14.5995, 120.9842))
            }
        },
        update = { mapView ->
            if (markersUpdated.value) {
                mapView.overlays.clear()

                reports.filter { it.verified } // ✅ Only show verified reports
                    .forEach { report ->
                        if (report.latitude != null && report.longitude != null) {
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(report.latitude, report.longitude)
                            marker.title = report.category
                            marker.snippet = report.description
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            // Optional: Set default icon if not set
                            if (marker.icon == null) {
                                marker.icon = ResourcesCompat.getDrawable(
                                    context.resources,
                                    org.osmdroid.library.R.drawable.marker_default,
                                    null
                                )
                            }

                            mapView.overlays.add(marker)
                        }
                    }

                mapView.invalidate()
                markersUpdated.value = false
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


// **Announcements** - Placeholder for announcements
@Composable
fun AnnouncementsScreen() {
    Text("Announcements Screen - Announcements list here")
}
