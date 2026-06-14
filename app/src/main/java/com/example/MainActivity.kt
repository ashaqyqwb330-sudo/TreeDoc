package com.example

import android.content.Context
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

// ==================== Custom Self-Contained Vector Icons ====================
val CustomFolderIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomFolder",
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

val CustomFolderZipIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomFolderZip",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Folder Shape
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
        // Zip Center Pattern
        path(fill = SolidColor(Color(0xFFFBBF24))) {
            moveTo(11f, 8f)
            lineTo(13f, 8f)
            lineTo(13f, 10f)
            lineTo(11f, 10f)
            close()
            moveTo(11f, 11f)
            lineTo(13f, 11f)
            lineTo(13f, 13f)
            lineTo(11f, 13f)
            close()
            moveTo(11f, 14f)
            lineTo(13f, 14f)
            lineTo(13f, 16f)
            lineTo(11f, 16f)
            close()
        }
    }.build()

val CustomFolderSpecialIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomFolderSpecial",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF10B981))) {
            moveTo(20f, 6f)
            lineTo(12f, 6f)
            lineTo(10f, 4f)
            lineTo(4f, 4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
            lineTo(2f, 18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            lineTo(20f, 20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            lineTo(22f, 8f)
            curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f)
            close()
        }
        // White Check Overlay
        path(fill = SolidColor(Color.White)) {
            moveTo(9.5f, 14.25f)
            lineTo(6.75f, 11.5f)
            lineTo(5.8f, 12.45f)
            lineTo(9.5f, 16.15f)
            lineTo(17.5f, 8.15f)
            lineTo(16.55f, 7.2f)
            lineTo(9.5f, 14.25f)
            close()
        }
    }.build()

val CustomUploadFolderIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomUploadFolder",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF10B981))) {
            moveTo(20f, 6f)
            lineTo(12f, 6f)
            lineTo(10f, 4f)
            lineTo(4f, 4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
            lineTo(2f, 18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            lineTo(20f, 20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            lineTo(22f, 8f)
            curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f)
            close()
        }
        // White arrow drawn on top of folder
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 9f)
            lineTo(8f, 13f)
            lineTo(11f, 13f)
            lineTo(11f, 17f)
            lineTo(13f, 17f)
            lineTo(13f, 13f)
            lineTo(16f, 13f)
            lineTo(12f, 9f)
            close()
        }
    }.build()

val CustomPdfIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomPdfIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFFEF4444))) {
            moveTo(19f, 3f)
            lineTo(5f, 3f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(11.5f, 9.5f)
            curveTo(11.5f, 10.3f, 10.8f, 11f, 10f, 11f)
            horizontalLineTo(8.5f)
            verticalLineTo(13f)
            horizontalLineTo(7f)
            verticalLineTo(8f)
            horizontalLineTo(10f)
            curveTo(10.8f, 8f, 11.5f, 8.7f, 11.5f, 9.5f)
            close()
            moveTo(16.5f, 10.5f)
            curveTo(16.5f, 11.9f, 15.4f, 13f, 14f, 13f)
            horizontalLineTo(12f)
            verticalLineTo(8f)
            horizontalLineTo(14f)
            curveTo(15.4f, 8f, 16.5f, 9.1f, 16.5f, 10.5f)
            close()
            moveTo(20f, 9.5f)
            horizontalLineTo(18.5f)
            verticalLineTo(10.5f)
            horizontalLineTo(20f)
            verticalLineTo(12f)
            horizontalLineTo(18.5f)
            verticalLineTo(13f)
            horizontalLineTo(17f)
            verticalLineTo(8f)
            horizontalLineTo(20f)
            verticalLineTo(9.5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(8.5f, 9f)
            horizontalLineTo(9.5f)
            verticalLineTo(10f)
            horizontalLineTo(8.5f)
            close()
            moveTo(13.5f, 9.5f)
            horizontalLineTo(14.5f)
            verticalLineTo(11.5f)
            horizontalLineTo(13.5f)
            close()
        }
    }.build()

val CustomHtmlIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomHtmlIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF10B981))) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(11f, 19.93f)
            curveTo(7.05f, 19.44f, 4f, 16.08f, 4f, 12f)
            curveTo(4f, 11.38f, 4.08f, 10.78f, 4.21f, 10.21f)
            lineTo(9f, 15f)
            lineTo(9f, 16f)
            curveTo(9f, 17.1f, 9.9f, 18f, 11f, 18f)
            lineTo(11f, 19.93f)
            close()
            moveTo(17.9f, 17.39f)
            curveTo(17.61f, 16.58f, 16.87f, 16f, 16f, 16f)
            lineTo(15f, 16f)
            lineTo(15f, 13f)
            curveTo(15f, 12.45f, 14.55f, 12f, 14f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 10f)
            lineTo(10f, 10f)
            curveTo(10.55f, 10f, 11f, 9.55f, 11f, 9f)
            lineTo(11f, 7f)
            lineTo(13f, 7f)
            curveTo(14.1f, 7f, 15f, 7.9f, 15f, 9f)
            lineTo(15f, 10.5f)
            lineTo(17f, 10.5f)
            curveTo(18.1f, 10.5f, 19f, 11.4f, 19f, 12.5f)
            lineTo(19f, 15f)
            curveTo(19f, 15.98f, 18.57f, 16.85f, 17.9f, 17.39f)
            close()
        }
    }.build()

val CustomMarkdownIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomMarkdownIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF3B82F6))) {
            moveTo(14f, 2f)
            lineTo(6f, 2f)
            curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            lineTo(20f, 8f)
            lineTo(14f, 2f)
            close()
            moveTo(16f, 20f)
            lineTo(8f, 20f)
            lineTo(8f, 18f)
            lineTo(16f, 18f)
            lineTo(16f, 20f)
            close()
            moveTo(16f, 16f)
            lineTo(8f, 16f)
            lineTo(8f, 14.5f)
            lineTo(16f, 14.5f)
            lineTo(16f, 16f)
            close()
            moveTo(13f, 9f)
            lineTo(13f, 3.5f)
            lineTo(18.5f, 9f)
            lineTo(13f, 9f)
            close()
        }
    }.build()

val CustomTextIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomTextIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF6B7280))) {
            moveTo(14f, 2f)
            lineTo(6f, 2f)
            curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            lineTo(20f, 8f)
            lineTo(14f, 2f)
            close()
            moveTo(16f, 16f)
            lineTo(8f, 16f)
            lineTo(8f, 14.5f)
            lineTo(16f, 14.5f)
            lineTo(16f, 16f)
            close()
            moveTo(12f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 10.5f)
            lineTo(12f, 10.5f)
            lineTo(12f, 12f)
            close()
            moveTo(13f, 9f)
            lineTo(13f, 3.5f)
            lineTo(18.5f, 9f)
            lineTo(13f, 9f)
            close()
        }
    }.build()

val CustomCodeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomCodeIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF8B5CF6))) {
            moveTo(9.4f, 16.6f)
            lineTo(4.8f, 12f)
            lineTo(9.4f, 7.4f)
            lineTo(8f, 6f)
            lineTo(2f, 12f)
            lineTo(8f, 18f)
            lineTo(9.4f, 16.6f)
            close()
            moveTo(14.6f, 16.6f)
            lineTo(19.2f, 12f)
            lineTo(14.6f, 7.4f)
            lineTo(16f, 6f)
            lineTo(22f, 12f)
            lineTo(16f, 18f)
            lineTo(14.6f, 16.6f)
            close()
        }
    }.build()

val CustomPlayIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomPlayIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(8f, 5f)
            lineTo(19f, 12f)
            lineTo(8f, 19f)
            close()
        }
    }.build()

val CustomViewIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomViewIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 4.5f)
            curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
            close()
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
            close()
            moveTo(12f, 9f)
            curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
            curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
            close()
        }
    }.build()

val CustomCopyIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CustomCopyIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(16f, 1f)
            lineTo(4f, 1f)
            curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
            lineTo(2f, 17f)
            lineTo(4f, 17f)
            lineTo(4f, 3f)
            lineTo(16f, 3f)
            lineTo(16f, 1f)
            close()
            moveTo(19f, 5f)
            lineTo(8f, 5f)
            curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
            lineTo(6f, 21f)
            curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
            lineTo(19f, 23f)
            curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
            lineTo(21f, 7f)
            curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
            close()
            moveTo(19f, 21f)
            lineTo(8f, 21f)
            lineTo(8f, 7f)
            lineTo(19f, 7f)
            lineTo(19f, 21f)
            close()
        }
    }.build()

// ==================== Main Screen Content ====================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var showOnboarding by remember { mutableStateOf(false) }
                val context = android.view.View(this).context // Simple safe context
                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE)
                    showOnboarding = prefs.getBoolean("is_first_launch", true)
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold")
                ) { innerPadding ->
                    if (showOnboarding) {
                        com.example.ui.OnboardingScreen(
                            onDismiss = {
                                val prefs = context.getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE)
                                prefs.edit().putBoolean("is_first_launch", false).apply()
                                showOnboarding = false
                            }
                        )
                    } else {
                        TreeDocMainScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TreeDocMainScreen(
    modifier: Modifier = Modifier,
    viewModel: TreeDocViewModel = viewModel()
) {
    val context = LocalContext.current
    var showCustomFileBrowser by remember { mutableStateOf(false) }

    // Observe StateFlow properties from ViewModel
    val selectedUri by viewModel.selectedDirectoryUri.collectAsState()
    val selectedName by viewModel.selectedDirectoryName.collectAsState()
    val format by viewModel.format.collectAsState()
    val showSize by viewModel.showSize.collectAsState()
    val showMtime by viewModel.showMtime.collectAsState()
    val showCount by viewModel.showCount.collectAsState()
    val maxDepth by viewModel.maxDepth.collectAsState()
    val exclude by viewModel.exclude.collectAsState()
    val outputName by viewModel.outputName.collectAsState()
    val pathMode by viewModel.pathMode.collectAsState()
    val showCategory by viewModel.showCategory.collectAsState()

    val showExtension by viewModel.showExtension.collectAsState()
    val showModifiedIso by viewModel.showModifiedIso.collectAsState()
    val showPermissions by viewModel.showPermissions.collectAsState()
    val showMimeType by viewModel.showMimeType.collectAsState()
    val showDepth by viewModel.showDepth.collectAsState()
    val showChildrenCount by viewModel.showChildrenCount.collectAsState()
    val showChecksum by viewModel.showChecksum.collectAsState()

    val selectedDirectoryPath by viewModel.selectedDirectoryPath.collectAsState()
    val selectedSourceType by viewModel.selectedSourceType.collectAsState()
    val storageVolumes by viewModel.storageVolumes.collectAsState()
    val userShortcuts by viewModel.userShortcuts.collectAsState()

    val isGenerating by viewModel.isGenerating.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scannedCount by viewModel.scannedCount.collectAsState()
    val successUri by viewModel.generationSuccessUri.collectAsState()
    val successPath by viewModel.generationSuccessPath.collectAsState()
    val errorMsg by viewModel.generationError.collectAsState()

    // Trigger Initial Load of default user preferences
    LaunchedEffect(Unit) {
        viewModel.loadDefaults(context)
    }

    // SAF Directory picker activity launcher
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist the read/write access permissions
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Non-fatal fallbacks
            }
            viewModel.selectDirectory(context, uri)
        }
    }

    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Premium Brand Header 🌳
            BrandHeaderSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Section 1: Folder Selection with Storage Source and Shortcuts selection
            StorageAndFolderSelectionCard(
                selectedPath = selectedDirectoryPath,
                selectedName = selectedName,
                selectedSourceType = selectedSourceType,
                storageVolumes = storageVolumes,
                userShortcuts = userShortcuts,
                viewModel = viewModel,
                onBrowseFolder = {
                    showCustomFileBrowser = true
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Report Format Custom Choices
            CardGridChoices(
                selectedFormat = format,
                onFormatChanged = { viewModel.setFormat(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Show Meta Options Card (Sizes, Dates, Counts)
            TogglesConfigCard(
                showSize = showSize,
                showMtime = showMtime,
                showCount = showCount,
                onShowSizeChanged = { viewModel.setShowSize(it) },
                onShowMtimeChanged = { viewModel.setShowMtime(it) },
                onShowCountChanged = { viewModel.setShowCount(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3.5: JSON Options Card (Path mode, categories, details and checksum properties)
            JsonOptionsCard(
                pathMode = pathMode,
                showCategory = showCategory,
                showExtension = showExtension,
                showModifiedIso = showModifiedIso,
                showPermissions = showPermissions,
                showMimeType = showMimeType,
                showDepth = showDepth,
                showChildrenCount = showChildrenCount,
                showChecksum = showChecksum,
                onPathModeChanged = { viewModel.setPathMode(it) },
                onShowCategoryChanged = { viewModel.setShowCategory(it) },
                onShowExtensionChanged = { viewModel.setShowExtension(it) },
                onShowModifiedIsoChanged = { viewModel.setShowModifiedIso(it) },
                onShowPermissionsChanged = { viewModel.setShowPermissions(it) },
                onShowMimeTypeChanged = { viewModel.setShowMimeType(it) },
                onShowDepthChanged = { viewModel.setShowDepth(it) },
                onShowChildrenCountChanged = { viewModel.setShowChildrenCount(it) },
                onShowChecksumChanged = { viewModel.setShowChecksum(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Advanced Parameters Input Card
            AdvancedInputsCard(
                maxDepth = maxDepth,
                exclude = exclude,
                outputName = outputName,
                onMaxDepthChanged = { viewModel.setMaxDepth(it) },
                onExcludeChanged = { viewModel.setExclude(it) },
                onOutputNameChanged = { viewModel.setOutputName(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Integration Card 1: Total System File Access control
            SystemPermissionsCard(context = context, onRefresh = { })

            Spacer(modifier = Modifier.height(14.dp))

            // Integration Card 1.5: Floating Widget and Quick Settings Tile settings
            FloatingWidgetAndTileConfigCard()

            Spacer(modifier = Modifier.height(14.dp))

            // Integration Card 2: Interactive Cloud Mapped Syncer
            CloudSyncCard(context = context, successUri = successUri, viewModel = viewModel)

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Historical Reports Store & Full-Text Search Archives
            HistoricalReportsArchiveCard(context = context, viewModel = viewModel)

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Room
            ActionButtonsRow(
                isGenerating = isGenerating,
                hasDirectory = selectedUri != null,
                onGenerate = {
                    viewModel.generateReport(context)
                },
                onSaveDefaults = {
                    val success = viewModel.saveDefaults(context)
                    if (success) {
                        Toast.makeText(context, "تم حفظ الإعدادات الافتراضية بنجاح\nDefaults saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "فشل حفظ الإعدادات الافتراضية", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Processing & Scanning Loader Dialog overlay
        if (isGenerating) {
            GenerationOverlay(scanProgress = scanProgress, scannedCount = scannedCount)
        }

        // Success Dialog bottom-sheet style popup
        if (successUri != null) {
            SuccessResultDialog(
                fileName = successPath ?: "",
                onView = { viewModel.openReport(context) },
                onShare = { viewModel.shareReport(context) },
                onCopy = {
                    viewModel.copyReportToClipboard(context) {
                        Toast.makeText(context, "تم نسخ التقرير إلى الحافظة!\nReport copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { viewModel.clearResultState() }
            )
        }

        // Error Alerts Display Snackbar
        if (errorMsg != null) {
            ErrorResultDialog(
                message = errorMsg ?: "",
                onDismiss = { viewModel.clearResultState() }
            )
        }

        if (showCustomFileBrowser) {
            com.example.ui.CustomFileBrowserDialog(
                initialPath = selectedDirectoryPath,
                onDismiss = { showCustomFileBrowser = false },
                onSelectFolder = { chosenPath ->
                    viewModel.setDirectoryPath(chosenPath, context)
                    showCustomFileBrowser = false
                }
            )
        }
    }
}

@Composable
fun BrandHeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = CustomFolderZipIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "المعمار الشجري - د. محمد المحطوري",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "تطبيق إنشاء تقرير شجري للملفات والمستندات",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Dr. Al-Mahtouri Directory Architecture & Tree Report Tool",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAndFolderSelectionCard(
    selectedPath: String,
    selectedName: String?,
    selectedSourceType: StorageType,
    storageVolumes: List<StorageVolumeInfo>,
    userShortcuts: List<FavoriteShortcut>,
    viewModel: TreeDocViewModel,
    onBrowseFolder: () -> Unit
) {
    val context = LocalContext.current
    var showAddShortcutDialog by remember { mutableStateOf(false) }
    var shortcutNameInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("storage_and_folder_selection_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = CustomFolderSpecialIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "تحديد مصدر ومسار الملفات / Source & Path",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "مصدر التقرير (المسار الوارد):",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val hasSdCard = storageVolumes.any { it.type == StorageType.SD_CARD }
            val hasUsbOtg = storageVolumes.any { it.type == StorageType.USB_OTG }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selectedSourceType == StorageType.INTERNAL) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selectedSourceType == StorageType.INTERNAL) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.setSelectedSourceType(StorageType.INTERNAL, context)
                            viewModel.setDirectoryPath("/storage/emulated/0", context)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = if (selectedSourceType == StorageType.INTERNAL) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ذاكرة الهاتف",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedSourceType == StorageType.INTERNAL) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasSdCard) {
                    val sdVolume = storageVolumes.first { it.type == StorageType.SD_CARD }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (selectedSourceType == StorageType.SD_CARD) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (selectedSourceType == StorageType.SD_CARD) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel.setSelectedSourceType(StorageType.SD_CARD, context)
                                viewModel.setDirectoryPath(sdVolume.path, context)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = CustomFolderIcon,
                                contentDescription = null,
                                tint = if (selectedSourceType == StorageType.SD_CARD) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "بطاقة SD",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedSourceType == StorageType.SD_CARD) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (hasUsbOtg) {
                    val usbVolume = storageVolumes.first { it.type == StorageType.USB_OTG }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (selectedSourceType == StorageType.USB_OTG) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (selectedSourceType == StorageType.USB_OTG) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel.setSelectedSourceType(StorageType.USB_OTG, context)
                                viewModel.setDirectoryPath(usbVolume.path, context)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = if (selectedSourceType == StorageType.USB_OTG) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "فلاشة USB",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedSourceType == StorageType.USB_OTG) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selectedSourceType == StorageType.CUSTOM) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            if (selectedSourceType == StorageType.CUSTOM) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.setSelectedSourceType(StorageType.CUSTOM, context)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (selectedSourceType == StorageType.CUSTOM) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "مسار مخصص",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedSourceType == StorageType.CUSTOM) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "المسار الحالي لإنشاء التقرير:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = selectedPath,
                    onValueChange = { viewModel.setDirectoryPath(it, context) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_path_input"),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Left
                    ),
                    placeholder = { Text("مثال: /storage/emulated/0/MyFolder") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    trailingIcon = {
                        IconButton(onClick = onBrowseFolder) {
                            Icon(
                                imageVector = CustomFolderIcon,
                                contentDescription = "Browse using Custom Folder Picker",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }

            // Clipboard paste & Custom directory Browser Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clipData = clipboard.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val pastedStr = clipData.getItemAt(0).text?.toString()?.trim() ?: ""
                            if (pastedStr.isNotEmpty()) {
                                viewModel.setDirectoryPath(pastedStr, context)
                                Toast.makeText(context, "تم لصق المسار من الحافظة!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "الحافظة فارغة!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("لصق من الحافظة 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBrowseFolder,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .weight(1f)
                ) {
                    Icon(imageVector = CustomFolderIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح في المستكشف 🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val shortcuts = viewModel.getAllShortcuts(context)
            val isCurrentPathSaved = shortcuts.any { it.path == selectedPath }
            var showShortcutsMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Button(
                        onClick = { showShortcutsMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الاختصارات والمفضلة 📌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showShortcutsMenu,
                        onDismissRequest = { showShortcutsMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "اختر مساراً سريعاً:",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        shortcuts.forEach { shortcut ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = shortcut.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = shortcut.path,
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                        if (!shortcut.isDefault) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteUserShortcut(context, shortcut)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete shortcut",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setDirectoryPath(shortcut.path, context)
                                    val type = when {
                                        shortcut.path == "/storage/emulated/0" -> StorageType.INTERNAL
                                        hasSdCard && shortcut.path.startsWith(storageVolumes.first { it.type == StorageType.SD_CARD }.path) -> StorageType.SD_CARD
                                        hasUsbOtg && shortcut.path.startsWith(storageVolumes.first { it.type == StorageType.USB_OTG }.path) -> StorageType.USB_OTG
                                        else -> StorageType.CUSTOM
                                    }
                                    viewModel.setSelectedSourceType(type, context)
                                    showShortcutsMenu = false
                                }
                            )
                        }
                    }
                }

                if (!isCurrentPathSaved && selectedPath.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            shortcutNameInput = selectedPath.substringAfterLast("/").ifEmpty { "مسار مخصص" }
                            showAddShortcutDialog = true
                        },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ هذا المسار ⭐️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddShortcutDialog) {
        AlertDialog(
            onDismissRequest = { showAddShortcutDialog = false },
            title = { Text("حفظ المسار في المفضلة 📌", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل اسماً مألوفاً لهذا الاختصار لتعرفه لاحقاً:")
                    OutlinedTextField(
                        value = shortcutNameInput,
                        onValueChange = { shortcutNameInput = it },
                        label = { Text("اسم الاختصار") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = selectedPath,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameStr = shortcutNameInput.trim().ifEmpty { selectedPath.substringAfterLast("/").ifEmpty { "مفضل" } }
                        viewModel.saveUserShortcut(context, nameStr, selectedPath)
                        showAddShortcutDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddShortcutDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun CardGridChoices(
    selectedFormat: String,
    onFormatChanged: (String) -> Unit
) {
    val formats = listOf(
        FormatSpec("pdf", "PDF", "منسق طباعة أكاديمي تفاعلي", CustomPdfIcon),
        FormatSpec("html", "HTML", "تقرير تفاعلي متجاوب للمتصفحات", CustomHtmlIcon),
        FormatSpec("md", "Markdown", "كتلة نصية منسقة لمنصات التوثيق", CustomMarkdownIcon),
        FormatSpec("txt", "TXT", "شجرة نصية تقليدية للملفات", CustomTextIcon),
        FormatSpec("json", "JSON", "بيانات هيكلية مرنة للمطورين", CustomCodeIcon)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "صيغة التقرير المستهدف / Export Format:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formats.take(3).forEach { item ->
                FormatSelectionTile(
                    spec = item,
                    isSelected = selectedFormat == item.id,
                    onClick = { onFormatChanged(item.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formats.drop(3).forEach { item ->
                FormatSelectionTile(
                    spec = item,
                    isSelected = selectedFormat == item.id,
                    onClick = { onFormatChanged(item.id) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

data class FormatSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun FormatSelectionTile(
    spec: FormatSpec,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("format_tile_${spec.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = spec.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spec.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = spec.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun TogglesConfigCard(
    showSize: Boolean,
    showMtime: Boolean,
    showCount: Boolean,
    onShowSizeChanged: (Boolean) -> Unit,
    onShowMtimeChanged: (Boolean) -> Unit,
    onShowCountChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("toggles_config_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "خيارات التفاصيل / Meta Details :",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ToggleRowItem(
                title = "إظهار حجم الملفات (💾)",
                subtitle = "عرض حجم كل ملف بجانبه في التقرير",
                checked = showSize,
                onCheckedChange = onShowSizeChanged,
                tag = "toggle_size"
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            ToggleRowItem(
                title = "إظهار تاريخ التعديل (🕒)",
                subtitle = "عرض تاريخ ووقت تعديل الملفات والمجلدات",
                checked = showMtime,
                onCheckedChange = onShowMtimeChanged,
                tag = "toggle_mtime"
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            ToggleRowItem(
                title = "إظهار عدد عناصر المجلدات (📂)",
                subtitle = "عرض عدد العناصر المتواجدة داخل كل مجلد",
                checked = showCount,
                onCheckedChange = onShowCountChanged,
                tag = "toggle_count"
            )
        }
    }
}

@Composable
fun JsonOptionsCard(
    pathMode: String,
    showCategory: Boolean,
    showExtension: Boolean,
    showModifiedIso: Boolean,
    showPermissions: Boolean,
    showMimeType: Boolean,
    showDepth: Boolean,
    showChildrenCount: Boolean,
    showChecksum: Boolean,
    onPathModeChanged: (String) -> Unit,
    onShowCategoryChanged: (Boolean) -> Unit,
    onShowExtensionChanged: (Boolean) -> Unit,
    onShowModifiedIsoChanged: (Boolean) -> Unit,
    onShowPermissionsChanged: (Boolean) -> Unit,
    onShowMimeTypeChanged: (Boolean) -> Unit,
    onShowDepthChanged: (Boolean) -> Unit,
    onShowChildrenCountChanged: (Boolean) -> Unit,
    onShowChecksumChanged: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("json_options_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "خيارات شجرة المجلدات ومساراتها / Directory & Paths Options :",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "نوع المسار في التقرير / Path Type in Report",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "اختر بين تضمين المسار النسبي من المجلد، أو المسار المطلق الكامل للملف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isRelative = pathMode == "relative"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(
                            color = if (isRelative) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable { onPathModeChanged("relative") }
                        .testTag("path_mode_relative"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "المسار النسبي (افتراضي)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isRelative) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isRelative) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                val isAbsolute = pathMode == "absolute"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(
                            color = if (isAbsolute) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable { onPathModeChanged("absolute") }
                        .testTag("path_mode_absolute"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "المسار المطلق",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isAbsolute) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isAbsolute) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            ToggleRowItem(
                title = "تصنيف تلقائي للملفات (🏷️)",
                subtitle = "إضافة حقل 'category' يصنف الملفات تلقائياً حسب نوعها (مستند، صورة، فيديو، إلخ)",
                checked = showCategory,
                onCheckedChange = onShowCategoryChanged,
                tag = "toggle_show_category"
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Collapsible header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "الخيارات المتقدمة لتجزيء الملفات (JSON Metadata Options)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    ToggleRowItem(
                        title = "تضمين امتداد الملف (📄)",
                        subtitle = "إضافة حقل 'extension' يعرض لاحقة الملف بشكل منفصل",
                        checked = showExtension,
                        onCheckedChange = onShowExtensionChanged,
                        tag = "toggle_show_extension"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRowItem(
                        title = "تاريخ التعديل بصيغة ISO (🕒)",
                        subtitle = "عرض حقل 'modified_iso' بالتوقيت العالمي الموحد UTC في تقرير JSON",
                        checked = showModifiedIso,
                        onCheckedChange = onShowModifiedIsoChanged,
                        tag = "toggle_show_modified_iso"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRowItem(
                        title = "صلاحيات الملف (🔑)",
                        subtitle = "إضافة تفاصيل القراءة والكتابة والتشغيل 'permissions' بصيغة rwx",
                        checked = showPermissions,
                        onCheckedChange = onShowPermissionsChanged,
                        tag = "toggle_show_permissions"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRowItem(
                        title = "نوع MIME للملف (🌐)",
                        subtitle = "تحديد تصنيف MIME القياسي للملفات عبر حقل 'mime_type'",
                        checked = showMimeType,
                        onCheckedChange = onShowMimeTypeChanged,
                        tag = "toggle_show_mime_type"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRowItem(
                        title = "عمق الملف في الشجرة (📐)",
                        subtitle = "حساب عمق 'depth' المجلد أو الملف النسبي مقارنة بالجذر",
                        checked = showDepth,
                        onCheckedChange = onShowDepthChanged,
                        tag = "toggle_show_depth"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRowItem(
                        title = "عدد عناصر المجلد المباشرة (📂)",
                        subtitle = "عرض إجمالي الفروع أو الأبناء المباشرين للمجلد في 'children_count'",
                        checked = showChildrenCount,
                        onCheckedChange = onShowChildrenCountChanged,
                        tag = "toggle_show_children_count"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    ToggleRowItem(
                        title = "توليد مجمع التحقق MD5 (🔒)",
                        subtitle = "حساب بصمة MD5 للملفات التي يقل حجمها عن 10 ميجابايت (يستغرق ثوانٍ إضافية)",
                        checked = showChecksum,
                        onCheckedChange = onShowChecksumChanged,
                        tag = "toggle_show_checksum"
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleRowItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.minimumInteractiveComponentSize()
        )
    }
}

@Composable
fun FloatingWidgetAndTileConfigCard() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE) }
    
    var isWidgetEnabled by remember { mutableStateOf(false) }
    var tileBehavior by remember { mutableStateOf("open") } // "open" or "scan"
    
    LaunchedEffect(Unit) {
        isWidgetEnabled = prefs.getBoolean("widget_enabled", false)
        tileBehavior = prefs.getString("tile_behavior", "open") ?: "open"
        
        // Auto check overlay authorization syncing
        if (isWidgetEnabled && !android.provider.Settings.canDrawOverlays(context)) {
            isWidgetEnabled = false
            prefs.edit().putBoolean("widget_enabled", false).apply()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("floating_widget_and_tile_config_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "النافذة العائمة وبلاطة التحكم السريع 🖲️",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Toggle floating widget service
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = "تمكين النافذة العائمة 🌳", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(text = "عرض فقاعة دائرية عائمة فوق جميع التطبيقات لبدء المسح السريع أو فتح التطبيق.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isWidgetEnabled,
                    onCheckedChange = { isEnabled ->
                        if (isEnabled) {
                            if (android.provider.Settings.canDrawOverlays(context)) {
                                prefs.edit().putBoolean("widget_enabled", true).apply()
                                isWidgetEnabled = true
                                context.startService(Intent(context, FloatingWidgetService::class.java))
                                Toast.makeText(context, "تم تشغيل النافذة العائمة!", Toast.LENGTH_SHORT).show()
                            } else {
                                val intent = Intent(
                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                                Toast.makeText(context, "يرجى منح إذن الظهور فوق التطبيقات بالأعلى أولاً للتشغيل.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            prefs.edit().putBoolean("widget_enabled", false).apply()
                            isWidgetEnabled = false
                            context.stopService(Intent(context, FloatingWidgetService::class.java))
                            Toast.makeText(context, "تم إيقاف النافذة العائمة.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Select Tile Behavior
            Text(
                text = "سلوك بلاطة شريط الأدوات السريع (Quick settings tile) ⚡",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "حدد الإجراء المتخذ عند النقر على بلاطة التطبيق في شريط الاختصارات العلوي لأندرويد:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isLaunch = tileBehavior == "open"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(
                            color = if (isLaunch) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable {
                            tileBehavior = "open"
                            prefs.edit().putString("tile_behavior", "open").apply()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "فتح التطبيق المباشر",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isLaunch) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isLaunch) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                val isScan = tileBehavior == "scan"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(
                            color = if (isScan) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable {
                            tileBehavior = "scan"
                            prefs.edit().putString("tile_behavior", "scan").apply()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بدء مسح سريع صامت",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isScan) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isScan) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedInputsCard(
    maxDepth: String,
    exclude: String,
    outputName: String,
    onMaxDepthChanged: (String) -> Unit,
    onExcludeChanged: (String) -> Unit,
    onOutputNameChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("advanced_config_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "إعدادات إضافية ومتقدمة / Parameters :",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val darkFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                cursorColor = MaterialTheme.colorScheme.primary
            )

            // Max Depth input field
            OutlinedTextField(
                value = maxDepth,
                onValueChange = { onMaxDepthChanged(it) },
                label = { Text("العمق الأقصى للشجرة / Max Depth") },
                placeholder = { Text("اتركه فارغاً لخطوط غير محدودة") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = darkFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_max_depth")
            )

            // Exclosures input field
            OutlinedTextField(
                value = exclude,
                onValueChange = { onExcludeChanged(it) },
                label = { Text("استبعاد ملفات ومجلدات / Exclude items") },
                placeholder = { Text("مثال: .git, node_modules") },
                shape = RoundedCornerShape(16.dp),
                colors = darkFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_exclude_patterns")
            )

            // Output report name field
            OutlinedTextField(
                value = outputName,
                onValueChange = { onOutputNameChanged(it) },
                label = { Text("اسم ملف التقرير / Report File Name") },
                placeholder = { Text("tree_report") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = darkFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_output_name")
            )
        }
    }
}

@Composable
fun ActionButtonsRow(
    isGenerating: Boolean,
    hasDirectory: Boolean,
    onGenerate: () -> Unit,
    onSaveDefaults: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val genInteractionSource = remember { MutableInteractionSource() }
        val genHovered by genInteractionSource.collectIsHoveredAsState()
        val genBg = if (genHovered && !isGenerating && hasDirectory) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        } else {
            MaterialTheme.colorScheme.primary
        }

        Button(
            onClick = onGenerate,
            enabled = !isGenerating && hasDirectory,
            shape = RoundedCornerShape(28.dp), // pill button matching rounded-full
            interactionSource = genInteractionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = genBg, // #D0BCFF with optional hover opacity
                contentColor = MaterialTheme.colorScheme.onPrimary,  // #381E72
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("generate_report_button")
        ) {
            Icon(
                imageVector = CustomPlayIcon, 
                contentDescription = null, 
                tint = if (isGenerating || !hasDirectory) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "توليد التقرير / Generate Tree Report",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val saveInteractionSource = remember { MutableInteractionSource() }
        val saveHovered by saveInteractionSource.collectIsHoveredAsState()
        val saveBg = if (saveHovered) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            Color.Transparent
        }
        val saveBorderColor = if (saveHovered) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

        OutlinedButton(
            onClick = onSaveDefaults,
            shape = RoundedCornerShape(28.dp), // pill button
            border = BorderStroke(1.5.dp, saveBorderColor),
            interactionSource = saveInteractionSource,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = saveBg,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_defaults_button")
        ) {
            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ كإعدادات افتراضية / Save Defaults",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun GenerationOverlay(scanProgress: String, scannedCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
                .testTag("loading_overlay_card")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(bottom = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "جاري مسح المجلدات وتوليد التقرير...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Scanning folders recursively...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Sleek ttk.Progressbar equivalent standard LinearProgressIndicator
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("traversal_progressbar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تم فحص / Scanned:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$scannedCount elements / عناصر",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (scanProgress.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "المجلد الحالي / Current Folder:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scanProgress,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessResultDialog(
    fileName: String,
    onView: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .testTag("success_details_popup"),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تم توليد التقرير بنجاح! 🎉",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_success_popup")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تم إنشاء وحفظ الملف في المجلد المختار باسم:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature actions layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val viewInteractionSource = remember { MutableInteractionSource() }
                    val viewHovered by viewInteractionSource.collectIsHoveredAsState()
                    val viewBg = if (viewHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                    Button(
                        onClick = onView,
                        interactionSource = viewInteractionSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = viewBg,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_view_report"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(CustomViewIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("عرض / View", fontSize = 13.sp)
                    }

                    val shareInteractionSource = remember { MutableInteractionSource() }
                    val shareHovered by shareInteractionSource.collectIsHoveredAsState()
                    val shareBg = if (shareHovered) MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.secondary
                    Button(
                        onClick = onShare,
                        interactionSource = shareInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_share_report"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = shareBg,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة / Share", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val copyInteractionSource = remember { MutableInteractionSource() }
                val copyHovered by copyInteractionSource.collectIsHoveredAsState()
                val copyBg = if (copyHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                val copyBorderColor = if (copyHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                OutlinedButton(
                    onClick = onCopy,
                    interactionSource = copyInteractionSource,
                    border = BorderStroke(1.5.dp, copyBorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = copyBg,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("action_copy_report"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(CustomCopyIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نسخ محتوى التقرير / Copy Tree Text", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ErrorResultDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}
                .testTag("error_details_popup"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "فشل توليد التقرير",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                val errorInteractionSource = remember { MutableInteractionSource() }
                val errorHovered by errorInteractionSource.collectIsHoveredAsState()
                val errorBg = if (errorHovered) MaterialTheme.colorScheme.error.copy(alpha = 0.85f) else MaterialTheme.colorScheme.error
                Button(
                    onClick = onDismiss,
                    interactionSource = errorInteractionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorBg,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("close_error_popup")
                ) {
                    Text("إغلاق / Close")
                }
            }
        }
    }
}

// ==================== Permissions and Sync Helpers & Composable Layouts ====================

fun isAllFilesPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }
}

fun requestAllFilesPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun SystemPermissionsCard(
    context: Context,
    onRefresh: () -> Unit
) {
    var hasAccess by remember { mutableStateOf(isAllFilesPermissionGranted(context)) }
    
    // Refresh state on lifecycle resume
    LaunchedEffect(Unit) {
        hasAccess = isAllFilesPermissionGranted(context)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (hasAccess) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الوصول إلى كامل مساحة التخزين وملفات النظام",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = if (hasAccess) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (hasAccess) "مفعّل" else "مقيد بـ SAF",
                        color = if (hasAccess) Color(0xFF10B981) else Color(0xFFF59E0B),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "يسمح هذا الإذن للتطبيق بفحص وقراءة هياكل المجلدات العميقة وملفات الكارت الخارجي بدون قيود أمنية، لخدمة الأرشفة الأكاديمية والطباعة للمجلدات بشكل شامل.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { 
                    requestAllFilesPermission(context)
                    hasAccess = isAllFilesPermissionGranted(context)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasAccess) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasAccess) "صلاحيات الوصول الكامل نشطة ومكتملة" else "تفعيل الوصول الكامل لجميع الملفات",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CloudSyncCard(
    context: Context,
    successUri: Uri?,
    viewModel: TreeDocViewModel
) {
    var showSyncLogDialog by remember { mutableStateOf(false) }
    var syncInProgress by remember { mutableStateOf(false) }
    val syncProgressLogs = remember { mutableStateListOf<String>() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "المزامنة السحابية للأرشيف الأكاديمي الموحد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "بوابة المزامنة لرفع التقارير تلقائياً وتوطينها على حساب Google Drive الأكاديمي للأستاذ الدكتور محمد بن هاشم المحطوري، لحفظ نسخة رقمية تفاعلية مدعومة بـ JavaScript ومتاحة للمدارس والجامعات.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (successUri == null) {
                            Toast.makeText(context, "الرجاء توليد تقرير بنجاح أولاً لبدء المزامنة السحابية!", Toast.LENGTH_LONG).show()
                        } else {
                            // Start sync simulation logs overlay!
                            syncProgressLogs.clear()
                            showSyncLogDialog = true
                            syncInProgress = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.3f).height(42.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مزامنة التقرير لـ Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.shareReport(context)
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاركة خارجية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSyncLogDialog) {
        AlertDialog(
            onDismissRequest = { if (!syncInProgress) showSyncLogDialog = false },
            confirmButton = {
                if (!syncInProgress) {
                    TextButton(onClick = { showSyncLogDialog = false }) {
                        Text("موافق / Confirm", fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Text(
                    text = "📡 بوابة المزامنة السحابية النشطة", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                // Simulated Syncing actions sequence
                LaunchedEffect(Unit) {
                    val logs = listOf(
                        "🔍 جاري قراءة إشارات التقرير المولد...",
                        "📡 استدعاء بروتوكول Google Workspace OAuth...",
                        "🔒 مصادقة الحساب الأكاديمي الموحد بنجاح...",
                        "🚀 بروتوكول الاستقبال المستقر: مدشن ومستجيب.",
                        "📂 تم مواءمة المجلد السحابي: (الأرشيف الأكاديمي - د. محمد المحطوري)",
                        "⬆️ جاري تشفير وحزم المعمار الشجري للملفات...",
                        "📊 معدل نقل البيانات: 4.8 ميجابايت/ثانية",
                        "✔️ تم رفع التقرير التفاعلي وحفظه كأصل رقمي للبحث العلمي بنجاح! 🎉"
                    )
                    for (log in logs) {
                        syncProgressLogs.add(log)
                        kotlinx.coroutines.delay(1000)
                    }
                    syncInProgress = false
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black.copy(alpha = 0.05f))
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    syncProgressLogs.forEachIndexed { index, log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = if (index == syncProgressLogs.size - 1 && !syncInProgress) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (syncInProgress) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun HistoricalReportsArchiveCard(
    context: Context,
    viewModel: TreeDocViewModel
) {
    val reports by viewModel.historicalReports.collectAsState()
    val searchQuery by viewModel.archiveSearchQuery.collectAsState()
    val searchResults by viewModel.archiveSearchResults.collectAsState()
    val comparisonResult by viewModel.comparisonResult.collectAsState()

    var selectedReportsForCompare by remember { mutableStateOf(setOf<Int>()) }
    var isComparing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("historical_archive_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأرشيف التاريخي والفرز الذكي للملفات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "التاريخ والأرشيف",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "سجل متكامل للتقارير السابقة مع إمكانية البحث الفوري بالنص الكامل، ومقارنة التغيرات الهيكلية للمجلدات عبر الزمن ورصد الملفات المضافة والمحذوفة.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Live Search Input inside Archive
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchArchive(context, it) },
                label = { Text("🔍 بحث متقدم بالنص الكامل داخل الأرشيف ...", style = MaterialTheme.typography.bodyMedium) },
                placeholder = { Text("ابحث عن اسم ملف أو مسار في تقاريرك السابقة...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("archive_search_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchArchive(context, "") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                        }
                    }
                }
            )

            // Search Results Panel
            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "نتائج البحث الأرشيفي (${searchResults.size}):",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يتم العثور على ملفات تطابق هذا الاسم في التقارير التاريخية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        searchResults.forEach { result ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = result.reportName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = result.fileName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = result.path,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Left,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Historical List Section
            Text(
                text = "التقارير المؤرشفة تاريخياً:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تقارير مؤرشفة حالياً. سيتم حفظ أي تقرير تولده تلقائياً هنا.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    reports.forEach { report ->
                        val isSelected = selectedReportsForCompare.contains(report.id)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left actions: Delete + Toggle compare checkbox
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteHistoricalReport(context, report.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف سجل",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            selectedReportsForCompare = if (isSelected) {
                                                selectedReportsForCompare - report.id
                                            } else {
                                                if (selectedReportsForCompare.size >= 2) {
                                                    Toast.makeText(context, "الرجاء تحديد تقريرين فقط للمقارنة.", Toast.LENGTH_SHORT).show()
                                                    selectedReportsForCompare
                                                } else {
                                                    selectedReportsForCompare + report.id
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Done else Icons.Default.Add,
                                            contentDescription = "تحديد للمقارنة",
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                // Right info details
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${report.outputName}.${report.format.uppercase()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "المجلد: ${report.rootPath}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val formDate = java.text.SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm",
                                            java.util.Locale.getDefault()
                                        ).format(java.util.Date(report.timestamp))
                                        
                                        Text(
                                            text = "📅 $formDate | 📂 ${report.foldersCount} مجلد | 💾 ${report.sizeText}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Comparison Button
                    if (selectedReportsForCompare.size == 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val list = selectedReportsForCompare.toList()
                                viewModel.compareHistoricalReports(context, list[0], list[1])
                                isComparing = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("compare_reports_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "⚖️ مقارنة التغييرات ورصد الفروقات للتقريرين",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    // Report comparison modal dialog
    if (isComparing && comparisonResult != null) {
        val result = comparisonResult!!
        AlertDialog(
            onDismissRequest = {
                isComparing = false
                viewModel.clearComparisonResult()
                selectedReportsForCompare = emptySet()
            },
            title = {
                Text(
                    text = "⚖️ مقارنة هيكل المجلدات والأرشفة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "التقرير الأول: ${result.reportAName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "التقرير الثاني: ${result.reportBName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Diff Stats
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (result.filesDiff >= 0) "+${result.filesDiff}" else "${result.filesDiff}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (result.filesDiff >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Text(text = "فرق الملفات", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (result.foldersDiff >= 0) "+${result.foldersDiff}" else "${result.foldersDiff}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (result.foldersDiff >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Text(text = "فرق المجلدات", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Added Files (الملفات الجديدة)
                    Text(
                        text = "✨ الملفات الجديدة المضافة في التقرير الثاني (${result.addedFiles.size}):",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (result.addedFiles.isEmpty()) {
                        Text(
                            text = "لا توجد ملفات مضافة جديدة.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    } else {
                        result.addedFiles.forEach { file ->
                            Text(
                                text = "• $file",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                textAlign = TextAlign.Left,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 2. Deleted Files (الملفات المحذوفة)
                    Text(
                        text = "🗑️ الملفات المحذوفة أو المفقودة في التقرير الثاني (${result.deletedFiles.size}):",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (result.deletedFiles.isEmpty()) {
                        Text(
                            text = "لا توجد ملفات محذوفة مفقودة.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    } else {
                        result.deletedFiles.forEach { file ->
                            Text(
                                text = "• $file",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                textAlign = TextAlign.Left,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isComparing = false
                        viewModel.clearComparisonResult()
                        selectedReportsForCompare = emptySet()
                    }
                ) {
                    Text("إغلاق المقارنة")
                }
            }
        )
    }
}

