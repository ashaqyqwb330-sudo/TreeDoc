package com.example

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class FloatingWidgetService : Service() {
    private lateinit var windowManager: WindowManager
    private var floatingLayout: LinearLayout? = null
    private var collapsedView: LinearLayout? = null
    private var expandedView: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (floatingLayout == null) {
            setupFloatingWidget()
        }
        return START_STICKY
    }

    private fun setupFloatingWidget() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 120
            y = 120
        }

        // 1. Create top-level parent layout
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // 2. Collapsed View: The Bubble (🌳)
        collapsedView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val bubbleD = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#D0BCFF")) // Sophisticated Dark Primary
            }
            background = bubbleD
            setPadding(16, 16, 16, 16)
        }
        val bubbleText = TextView(this).apply {
            text = "🌳"
            textSize = 22f
            setTextColor(Color.parseColor("#381E72"))
        }
        collapsedView?.addView(bubbleText)

        // 3. Expanded View: Command options list
        expandedView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            
            val cardD = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#2B2930")) // Sophisticated surface
                setStroke(2, Color.parseColor("#D0BCFF"))
            }
            background = cardD
            setPadding(24, 16, 24, 16)
        }

        val scanButton = Button(this).apply {
            text = "مسح سريع 📁"
            textSize = 11f
            setTextColor(Color.BLACK)
            val btnD = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#10B981")) // Green
            }
            background = btnD
            setPadding(16, 8, 16, 8)
            setOnClickListener {
                expandedView?.visibility = View.GONE
                collapsedView?.visibility = View.VISIBLE
                triggerSilentQuickScan()
            }
        }

        val openButton = Button(this).apply {
            text = "فتح التطبيق 📱"
            textSize = 11f
            setTextColor(Color.WHITE)
            val btnD = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#381E72"))
            }
            background = btnD
            setPadding(16, 8, 16, 8)
            setOnClickListener {
                expandedView?.visibility = View.GONE
                collapsedView?.visibility = View.VISIBLE
                openMainApplication()
            }
        }

        val closeButton = Button(this).apply {
            text = "إغلاق ❌"
            textSize = 10f
            setTextColor(Color.WHITE)
            val btnD = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#EF4444")) // Red
            }
            background = btnD
            setPadding(12, 8, 12, 8)
            setOnClickListener {
                stopSelf()
            }
        }

        // Assemble expanded widgets
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(10, 0, 10, 0)
        }
        
        expandedView?.addView(scanButton, lp)
        expandedView?.addView(openButton, lp)
        expandedView?.addView(closeButton, lp)

        // Add both to core container
        container.addView(collapsedView)
        container.addView(expandedView)

        // Drag/move gestures tracking
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isMoving = true
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(container, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoving) {
                        // Handle toggle collapse / expand state
                        if (expandedView?.visibility == View.GONE) {
                            collapsedView?.visibility = View.GONE
                            expandedView?.visibility = View.VISIBLE
                        } else {
                            expandedView?.visibility = View.GONE
                            collapsedView?.visibility = View.VISIBLE
                        }
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(container, params)
        floatingLayout = container
    }

    private fun triggerSilentQuickScan() {
        Toast.makeText(this, "جاري بدء المسح السريع بالخلفية...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE)
                val rawPath = prefs.getString("selected_directory_path", "/storage/emulated/0") ?: "/storage/emulated/0"
                val outputName = prefs.getString("output_name", "tree_report") ?: "tree_report"
                val format = prefs.getString("format", "html") ?: "html"
                
                val file = File(rawPath)
                if (!file.exists() || !file.isDirectory) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FloatingWidgetService, "خطأ: مسار المسح غير صالح.", Toast.LENGTH_LONG).show()
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
                                context = this@FloatingWidgetService,
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
                        Toast.makeText(
                            this@FloatingWidgetService, 
                            "المعمار الشجري: تم تحديث التقرير السريع بنجاح وتخزينه في:\n${destFile.name}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FloatingWidgetService, "لم يتم العثور على ملفات للمسح.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FloatingWidgetService, "فشل المسح السريع: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openMainApplication() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingLayout != null) {
            windowManager.removeView(floatingLayout)
            floatingLayout = null
        }
    }
}
