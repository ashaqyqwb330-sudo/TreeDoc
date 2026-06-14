package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor

val CustomFolderLocalIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomFolderLocal",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF10B981))) {
            moveTo(10f, 4f)
            lineTo(4f, 4f)
            curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
            lineTo(2f, 18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            lineTo(20f, 20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            lineTo(22f, 8f)
            curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f)
            lineTo(12f, 6f)
            lineTo(10f, 4f)
            close()
        }
    }.build()

val CustomFileDeviceIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomFileDevice",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF6B7280))) {
            moveTo(14f, 2f)
            lineTo(6f, 2f)
            curveTo(4.9f, 2f, 4.01f, 2.9f, 4.01f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.1f, 4.89f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            lineTo(20f, 8f)
            lineTo(14f, 2f)
            close()
            moveTo(13f, 9f)
            lineTo(13f, 3.5f)
            lineTo(18.5f, 9f)
            lineTo(13f, 9f)
            close()
        }
    }.build()

val CustomArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(8.59f, 16.59f)
            lineTo(13.17f, 12f)
            lineTo(8.59f, 7.41f)
            lineTo(10f, 6f)
            lineTo(16f, 12f)
            lineTo(10f, 18f)
            close()
        }
    }.build()

val CustomHelpCircleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomHelpCircle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF3B82F6))) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12.5f, 18f)
            lineTo(11.5f, 18f)
            lineTo(11.5f, 16f)
            lineTo(12.5f, 16f)
            close()
        }
    }.build()

val CustomContentPasteIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomContentPaste",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(19f, 2f)
            lineTo(14.82f, 2f)
            curveTo(14.4f, 0.84f, 13.3f, 0f, 12f, 0f)
            curveTo(10.7f, 0f, 9.6f, 0.84f, 9.18f, 2f)
            lineTo(5f, 2f)
            curveTo(3.9f, 2f, 3f, 2.9f, 3f, 4f)
            lineTo(3f, 20f)
            curveTo(3f, 21.1f, 3.9f, 22f, 5f, 22f)
            lineTo(19f, 22f)
            curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
            lineTo(21f, 4f)
            curveTo(21f, 2.9f, 20.1f, 2f, 19f, 2f)
            close()
            moveTo(12f, 2f)
            curveTo(12.55f, 2f, 13f, 2.45f, 13f, 3f)
            curveTo(13f, 3.55f, 12.55f, 4f, 12f, 4f)
            curveTo(11.45f, 4f, 11f, 3.55f, 11f, 3f)
            curveTo(11f, 2.45f, 11.45f, 2f, 12f, 2f)
            close()
        }
    }.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFileBrowserDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onSelectFolder: (String) -> Unit
) {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf(if (initialPath.isEmpty()) "/storage/emulated/0" else initialPath) }
    var fileItems by remember { mutableStateOf<List<File>>(emptyList()) }
    var showHidden by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Fallback/Access limits states
    var showAccessFailedDialog by remember { mutableStateOf(false) }
    var failedPathAttempted by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Read files in current directory
    fun refreshFiles() {
        try {
            val dir = File(currentPath)
            if (dir.exists() && dir.isDirectory) {
                val list = dir.listFiles()
                if (list == null) {
                    // System limits access (Android 11+ security constraint on Android/data etc.)
                    failedPathAttempted = currentPath
                    showAccessFailedDialog = true
                    fileItems = emptyList()
                } else {
                    val filtered = list.filter { file ->
                        val matchesSearch = if (searchQuery.isNotEmpty()) {
                            file.name.contains(searchQuery, ignoreCase = true)
                        } else true
                        
                        val matchesHidden = if (!showHidden) {
                            !file.name.startsWith(".")
                        } else true
                        
                        matchesSearch && matchesHidden
                    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    fileItems = filtered
                }
            } else {
                fileItems = emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fileItems = emptyList()
        }
    }

    LaunchedEffect(currentPath, searchQuery, showHidden) {
        refreshFiles()
    }

    // Common shortcuts
    val shortcuts = listOf(
        Pair("الرئيسي 🏠", "/storage/emulated/0"),
        Pair("Android/data ⚙️", "/storage/emulated/0/Android/data"),
        Pair("Android/obb 🎮", "/storage/emulated/0/Android/obb"),
        Pair("التحميلات ⬇️", "/storage/emulated/0/Download"),
        Pair("الكاميرا 📸", "/storage/emulated/0/DCIM"),
        Pair("المستندات 📄", "/storage/emulated/0/Documents"),
        Pair("الصور 🖼️", "/storage/emulated/0/Pictures"),
        Pair("الموسيقى 🎵", "/storage/emulated/0/Music"),
        Pair("الأفلام 🎬", "/storage/emulated/0/Movies")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1C24),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header View 📂
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CustomFolderLocalIcon,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "متصفح المجلدات الاحترافي 🌳",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD0BCFF)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "قفل", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Path and Clipboard Tools
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = currentPath,
                        onValueChange = { currentPath = it },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Left
                        ),
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("المسار المطلق...", fontSize = 11.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2B2930),
                            unfocusedContainerColor = Color(0xFF2B2930),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFFD0BCFF)
                        )
                    )

                    // Paste clipboard helper
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = clipboard.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val pastedStr = clipData.getItemAt(0).text?.toString()?.trim() ?: ""
                                if (pastedStr.isNotEmpty()) {
                                    currentPath = pastedStr
                                    Toast.makeText(context, "تم لصق المسار من الحافظة!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "الحافظة فارغة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF49454F),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = CustomContentPasteIcon, contentDescription = "لصق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation controls & searching row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back arrow/Up folder
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val parent = File(currentPath).parentFile
                                if (parent != null) {
                                    currentPath = parent.absolutePath
                                }
                            },
                            enabled = File(currentPath).parent != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF381E72),
                                contentColor = Color(0xFFD0BCFF)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "لأعلى", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مستوى لأعلى (..)", fontSize = 11.sp)
                        }
                    }

                    // Show hidden files checkbox / toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showHidden = !showHidden }
                    ) {
                        Checkbox(
                            checked = showHidden,
                            onCheckedChange = { showHidden = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD0BCFF))
                        )
                        Text("إظهار الملفات المخفية", fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Shortcuts grid layout (Horizontal navigation)
                Text(
                    text = "الاختصارات السريعة لمجلدات أندرويد 🔗",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF).copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val chunkedShortcuts = shortcuts.chunked(3)
                        items(chunkedShortcuts) { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chunk.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(8.dp))
                                            .background(Color(0xFF2B2930), RoundedCornerShape(8.dp))
                                            .clickable { currentPath = item.second }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.first,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                if (chunk.size < 3) {
                                    repeat(3 - chunk.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = Color(0xFF49454F), modifier = Modifier.padding(bottom = 8.dp))

                // Search field inside directory files
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في المجلد الحالي...", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF24222A),
                        unfocusedContainerColor = Color(0xFF24222A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Files / Directory list View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp))
                        .background(Color(0xFF24222A), RoundedCornerShape(16.dp))
                        .padding(8.dp)
                ) {
                    if (fileItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "مجلد فارغ أو لا توجد صلاحيات لعرض الملفات\n(Empty or access limits folder)",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(fileItems) { file ->
                                val isDirectory = file.isDirectory
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2B2930))
                                        .clickable {
                                            if (isDirectory) {
                                                currentPath = file.absolutePath
                                            } else {
                                                Toast.makeText(context, "الرجاء تحديد مجلد فقط!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDirectory) CustomFolderLocalIcon else CustomFileDeviceIcon,
                                        contentDescription = null,
                                        tint = if (isDirectory) Color(0xFF10B981) else Color(0xFF3B82F6),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        
                                        // Metadata labels
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!isDirectory) {
                                                val len = file.length()
                                                val readableSize = when {
                                                    len >= 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f GB", len / (1024f * 1024f * 1024f))
                                                    len >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", len / (1024f * 1024f))
                                                    len >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", len / 1024f)
                                                    else -> "$len B"
                                                }
                                                Text(text = readableSize, fontSize = 10.sp, color = Color.LightGray)
                                            } else {
                                                val listSz = file.list()?.size ?: 0
                                                Text(text = "($listSz عناصر)", fontSize = 10.sp, color = Color(0xFF10B981))
                                            }
                                            
                                            val mtime = file.lastModified()
                                            if (mtime > 0) {
                                                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(mtime))
                                                Text(text = dateStr, fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    if (isDirectory) {
                                        Icon(imageVector = CustomArrowIcon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom help and control labels
                Text(
                    text = "💡 إذا لم يظهر المجلد الذي تريده، جرّب لصق مساره كاملاً أو استخدم الاختصارات السريعة بالأعلى.",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Save or select choice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSelectFolder(currentPath) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تحديد هذا المجلد 📁", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.weight(0.8f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF49454F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = CustomHelpCircleIcon, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مساعدة")
                    }
                }
            }
        }
    }

    // Access failed recovery dialog (Android 11+ limit helper)
    if (showAccessFailedDialog) {
        AlertDialog(
            onDismissRequest = { showAccessFailedDialog = false },
            title = {
                Text(
                    text = "🔒 قيد حماية بالوصول (أندرويد 11+)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Red
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يمنع أندرويد 11+ تصفح مجلدات Android/data مباشرة عبر listFiles بسبب قيود الأمان الاستثنائية.",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "المسار المستهدف:\n$failedPathAttempted",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFD0BCFF)
                    )
                    Text(
                        text = "هنا هي الخيارات المتاحة للتجربة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessFailedDialog = false
                        // Propose alternative pathways
                        val alternPath = when {
                            failedPathAttempted.contains("/storage/emulated/0/") -> failedPathAttempted.replace("/storage/emulated/0/", "/sdcard/")
                            failedPathAttempted.contains("/sdcard/") -> failedPathAttempted.replace("/sdcard/", "/mnt/sdcard/")
                            failedPathAttempted.contains("/mnt/sdcard/") -> failedPathAttempted.replace("/mnt/sdcard/", "/storage/emulated/0/")
                            else -> "/sdcard/Android/data"
                        }
                        currentPath = alternPath
                        Toast.makeText(context, "المحاولة بمسار بديل: $alternPath", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("المحاولة بمسار بديل")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAccessFailedDialog = false
                        showHelpDialog = true
                    }
                ) {
                    Text("عرض المساعدة ℹ️")
                }
            }
        )
    }

    // Help Dialog with built-in guidelines
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "مساعدة تصفح وتخطي الأذونات ℹ️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD0BCFF)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "كيف أصل إلى مجلد Android/data؟",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "1. افتح الإعدادات وفعل إذن 'الوصول لإدارة جميع الملفات' (MANAGE_EXTERNAL_STORAGE).\n" +
                                   "2. تصفح المسارات البديلة التي قد ترفع الحظر في جهازك تلقائياً.\n" +
                                   "3. الصق مسار أي مجلد فرعي بالكامل في الحقل العلوي مباشرة لتجاوز حظر المجلد الأم.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تصفح الفلاشات و بطاقات الذاكرة OTGs:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "عند شبك فلاشة تخزين خارجية، تظهر عادة ضمن المسار:\n/storage/XXXX-XXXX/\nأو /mnt/media_rw/.\nيمكنك تجربة كتابته يدوياً إن لم نكتشفه.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("فهمت")
                }
            }
        )
    }
}
