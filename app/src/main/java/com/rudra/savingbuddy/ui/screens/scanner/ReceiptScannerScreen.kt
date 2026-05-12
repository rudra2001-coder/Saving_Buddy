package com.rudra.savingbuddy.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.rudra.savingbuddy.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ScannedData(
    val amount: Double = 0.0,
    val merchant: String = "",
    val date: Long = System.currentTimeMillis(),
    val category: String = "Others",
    val items: List<String> = emptyList(),
    val isProcessing: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerScreen(
    onNavigateBack: () -> Unit = {},
    onScanned: (ScannedData) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scannedData by remember { mutableStateOf<ScannedData?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showScanResult by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    fun processImage(uri: Uri) {
        isProcessing = true
        errorMessage = null
        scope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                if (bitmap == null) {
                    errorMessage = "Failed to decode image"
                    isProcessing = false
                    return@launch
                }
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = withContext(Dispatchers.Default) {
                    Tasks.await(recognizer.process(image))
                }
                val text = result.text
                val lines = text.lines().filter { it.isNotBlank() }

                val amountPattern = Regex("[\\d,]+\\.?\\d{0,2}")
                var extractedAmount = 0.0
                var extractedMerchant = ""
                val extractedItems = mutableListOf<String>()

                for (line in lines.drop(1)) {
                    val cleaned = line.trim()
                    val hasAmount = amountPattern.find(cleaned)

                    if (hasAmount != null) {
                        val numStr = hasAmount.value.replace(",", "")
                        val num = numStr.toDoubleOrNull()
                        if (num != null && num > 0) {
                            if (num > extractedAmount) {
                                extractedAmount = num
                            }
                            val itemName = cleaned.replace(Regex("[\\d,]+\\.?\\d{0,2}\\s*"), "").trim()
                            if (itemName.isNotBlank()) {
                                extractedItems.add(" - ")
                            } else {
                                extractedItems.add("Item - ")
                            }
                        }
                    }

                    if (extractedMerchant.isBlank() && cleaned.length > 3 && cleaned.none { it.isDigit() }) {
                        extractedMerchant = cleaned
                    }
                }

                if (lines.isNotEmpty() && extractedMerchant.isBlank()) {
                    extractedMerchant = lines.first().trim().take(50)
                }

                scannedData = ScannedData(
                    amount = if (extractedAmount > 0) extractedAmount else text.split("\n").firstOrNull { it.contains(Regex("[\\d]")) }?.let {
                        Regex("\\d+\\.?\\d{0,2}").find(it)?.value?.toDoubleOrNull()
                    } ?: 0.0,
                    merchant = extractedMerchant.ifBlank { "Unknown Store" },
                    date = System.currentTimeMillis(),
                    category = guessCategory(text),
                    items = extractedItems.ifEmpty {
                        lines.filter { it.length > 5 }.take(5)
                    },
                    isProcessing = false
                )
                isProcessing = false
                showScanResult = true
            } catch (e: Exception) {
                errorMessage = "Recognition failed: "
                isProcessing = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            processImage(capturedImageUri!!)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            capturedImageUri = uri
            processImage(uri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = createImageFile(context)
            capturedImageUri = FileProvider.getUriForFile(
                context,
                ".fileprovider",
                photoFile
            )
            cameraLauncher.launch(capturedImageUri!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing receipt...", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
            } else if (errorMessage != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Outlined.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = ExpenseRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumButton(text = "Try Again", onClick = { errorMessage = null })
                }
            } else if (showScanResult && scannedData != null) {
                ScanResultView(
                    data = scannedData!!,
                    onDismiss = {
                        showScanResult = false
                        scannedData = null
                    },
                    onConfirm = {
                        onScanned(scannedData!!)
                        onNavigateBack()
                    },
                    onRescan = {
                        showScanResult = false
                        scannedData = null
                    }
                )
            } else {
                ScannerHomeView(
                    onScan = {
                        when {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                                val photoFile = createImageFile(context)
                                capturedImageUri = FileProvider.getUriForFile(
                                    context,
                                    ".fileprovider",
                                    photoFile
                                )
                                cameraLauncher.launch(capturedImageUri!!)
                            }
                            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPickFromGallery = {
                        galleryLauncher.launch("image/*")
                    }
                )
            }
        }
    }
}

private fun guessCategory(text: String): String {
    val lower = text.lowercase()
    return when {
        "grocery" in lower || "supermarket" in lower || "food" in lower || "restaurant" in lower -> "FOOD"
        "gas" in lower || "fuel" in lower || "petrol" in lower || "uber" in lower || "lyft" in lower -> "TRANSPORT"
        "amazon" in lower || "walmart" in lower || "target" in lower || "best buy" in lower || "mall" in lower -> "SHOPPING"
        "electric" in lower || "water" in lower || "utility" in lower || "internet" in lower || "phone" in lower -> "BILLS"
        "movie" in lower || "netflix" in lower || "spotify" in lower || "game" in lower -> "ENTERTAINMENT"
        "medical" in lower || "pharmacy" in lower || "doctor" in lower || "hospital" in lower || "health" in lower -> "HEALTH"
        else -> "OTHERS"
    }
}

@Composable
private fun ScannerHomeView(
    onScan: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colors = listOf(PrimaryGreen.copy(alpha = 0.3f), AccentCyan.copy(alpha = 0.2f)))
                )
                .border(2.dp, PrimaryGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.CameraAlt,
                null,
                tint = PrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Scan a Receipt",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Take a photo of your receipt and we will automatically extract the amount, merchant, and category",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        PremiumButton(
            text = "Take Photo",
            onClick = onScan,
            icon = Icons.Default.CameraAlt,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        PremiumOutlineButton(
            text = "Pick from Gallery",
            onClick = onPickFromGallery,
            icon = Icons.Default.PhotoLibrary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
            border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("How it works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                listOf("Take a clear photo of your receipt", "ML Kit extracts text & amounts", "Auto-fills expense details").forEachIndexed { i, step ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Surface(color = PrimaryGreen.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(step, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanResultView(
    data: ScannedData,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onRescan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(colors = listOf(PrimaryGreen.copy(alpha = 0.2f), AccentCyan.copy(alpha = 0.15f), BackgroundCard)))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = PrimaryGreen, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Receipt Scanned!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text("Data extracted successfully", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Receipt, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extracted Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ScannedFieldRow("Merchant", data.merchant)
                    ScannedFieldRow("Amount", "৳")
                    ScannedFieldRow("Category", data.category)
                    ScannedFieldRow("Date", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(data.date)))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.List, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    data.items.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("•  ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumOutlineButton(
                    text = "Rescan",
                    onClick = onRescan,
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f)
                )
                PremiumButton(
                    text = "Add Expense",
                    onClick = onConfirm,
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScannedFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

private fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageDir = File(context.cacheDir, "receipts")
    imageDir.mkdirs()
    return File(imageDir, "receipt_.jpg")
}
