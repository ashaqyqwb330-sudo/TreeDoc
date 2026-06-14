package com.example

import android.service.quicksettings.TileService
import android.content.Intent
import android.content.Context
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class TreeDocTileService : TileService() {

    override fun onClick() {
        val prefs = getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE)
        val behavior = prefs.getString("tile_behavior", "open") ?: "open" // "open" or "scan"

        if (behavior == "scan") {
            // Trigger automatic background silent scan
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "البدء بالمسح المتسلسل السريع...", Toast.LENGTH_SHORT).show()
            }
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val rawPath = prefs.getString("selected_directory_path", "/storage/emulated/0") ?: "/storage/emulated/0"
                    val outputName = prefs.getString("output_name", "tree_report") ?: "tree_report"
                    val format = prefs.getString("format", "html") ?: "html"
                    
                    val file = File(rawPath)
                    if (!file.exists() || !file.isDirectory) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TreeDocTileService, "خطأ بالمسح: مسار غير صالح.", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }

                    val ext = when (format) {
                        "html" -> "html"
                        "md" -> "md"
                        "json" -> "json"
                        "pdf" -> "pdf"
                        else -> "txt"
                    }
                    val targetFileName = "$outputName.$ext"
                    val destFile = File(rawPath, targetFileName)

                    val showSize = prefs.getBoolean("show_size", true)
                    val showMtime = prefs.getBoolean("show_mtime", false)
                    val showCount = prefs.getBoolean("show_count", false)
                    val maxDepthStr = prefs.getString("max_depth", "") ?: ""
                    val maxDepth = maxDepthStr.toIntOrNull()
                    val excludePatterns = (prefs.getString("exclude", "") ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val showChecksum = prefs.getBoolean("show_checksum", false)

                    val counter = AtomicInteger(0)
                    val rootNode = TreeReportGenerator.buildTreeFromFile(
                        file = file,
                        depth = 0,
                        maxDepth = maxDepth,
                        excludeList = excludePatterns,
                        showSize = showSize,
                        showMtime = showMtime,
                        showCount = showCount,
                        scannedCount = counter,
                        computeChecksum = showChecksum,
                        onProgress = { _, _ -> }
                    )

                    if (rootNode != null) {
                        java.io.FileOutputStream(destFile).use { outStream ->
                            if (format == "pdf") {
                                TreeReportGenerator.writePdf(
                                    context = this@TreeDocTileService,
                                    node = rootNode,
                                    outStream = outStream,
                                    rootName = file.name,
                                    showSize = showSize,
                                    showMtime = showMtime,
                                    showCount = showCount
                                )
                            } else {
                                BufferedWriter(OutputStreamWriter(outStream, "UTF-8")).use { writer ->
                                    when (format) {
                                        "html" -> TreeReportGenerator.writeHtml(rootNode, writer, file.name, showSize, showMtime, showCount)
                                        "md" -> TreeReportGenerator.writeMarkdown(rootNode, writer, file.name, showSize, showMtime, showCount)
                                        "json" -> {
                                            val pathMode = prefs.getString("path_mode", "relative") ?: "relative"
                                            val showCategory = prefs.getBoolean("show_category", false)
                                            val showExtension = prefs.getBoolean("show_extension", false)
                                            val showModifiedIso = prefs.getBoolean("show_modified_iso", false)
                                            val showPermissions = prefs.getBoolean("show_permissions", false)
                                            val showMimeType = prefs.getBoolean("show_mime_type", false)
                                            val showDepth = prefs.getBoolean("show_depth", false)
                                            val showChildrenCount = prefs.getBoolean("show_children_count", false)
                                            TreeReportGenerator.writeJson(
                                                node = rootNode,
                                                outWriter = writer,
                                                rootName = file.name,
                                                showSize = showSize,
                                                showMtime = showMtime,
                                                showCount = showCount,
                                                pathMode = pathMode,
                                                showCategory = showCategory,
                                                showExtension = showExtension,
                                                showModifiedIso = showModifiedIso,
                                                showPermissions = showPermissions,
                                                showMimeType = showMimeType,
                                                showDepth = showDepth,
                                                showChildrenCount = showChildrenCount,
                                                showChecksum = showChecksum
                                            )
                                        }
                                        else -> TreeReportGenerator.writeTxt(rootNode, writer, file.name, showSize, showMtime, showCount)
                                    }
                                }
                            }
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TreeDocTileService, "تم تحديث التقرير الشجري الصامت بنجاح بنهاية المسار!", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TreeDocTileService, "خطأ بالمسح الصامت: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            // Open the application directly
            try {
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
