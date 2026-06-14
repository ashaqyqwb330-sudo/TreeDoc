package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.TreeReportGenerator.TreeDocNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class TreeDocViewModel : ViewModel() {

    // Target storage directory states
    private val _selectedDirectoryUri = MutableStateFlow<Uri?>(null)
    val selectedDirectoryUri: StateFlow<Uri?> = _selectedDirectoryUri.asStateFlow()

    private val _selectedDirectoryName = MutableStateFlow<String?>(null)
    val selectedDirectoryName: StateFlow<String?> = _selectedDirectoryName.asStateFlow()

    // Config options
    private val _format = MutableStateFlow("html")
    val format: StateFlow<String> = _format.asStateFlow()

    private val _showSize = MutableStateFlow(true)
    val showSize: StateFlow<Boolean> = _showSize.asStateFlow()

    private val _showMtime = MutableStateFlow(false)
    val showMtime: StateFlow<Boolean> = _showMtime.asStateFlow()

    private val _showCount = MutableStateFlow(false)
    val showCount: StateFlow<Boolean> = _showCount.asStateFlow()

    private val _maxDepth = MutableStateFlow("")
    val maxDepth: StateFlow<String> = _maxDepth.asStateFlow()

    private val _exclude = MutableStateFlow(".git, __pycache__, node_modules")
    val exclude: StateFlow<String> = _exclude.asStateFlow()

    private val _outputName = MutableStateFlow("tree_report")
    val outputName: StateFlow<String> = _outputName.asStateFlow()

    private val _pathMode = MutableStateFlow("relative")
    val pathMode: StateFlow<String> = _pathMode.asStateFlow()

    private val _showCategory = MutableStateFlow(false)
    val showCategory: StateFlow<Boolean> = _showCategory.asStateFlow()

    private val _showExtension = MutableStateFlow(false)
    val showExtension: StateFlow<Boolean> = _showExtension.asStateFlow()

    private val _showModifiedIso = MutableStateFlow(false)
    val showModifiedIso: StateFlow<Boolean> = _showModifiedIso.asStateFlow()

    private val _showPermissions = MutableStateFlow(false)
    val showPermissions: StateFlow<Boolean> = _showPermissions.asStateFlow()

    private val _showMimeType = MutableStateFlow(false)
    val showMimeType: StateFlow<Boolean> = _showMimeType.asStateFlow()

    private val _showDepth = MutableStateFlow(false)
    val showDepth: StateFlow<Boolean> = _showDepth.asStateFlow()

    private val _showChildrenCount = MutableStateFlow(false)
    val showChildrenCount: StateFlow<Boolean> = _showChildrenCount.asStateFlow()

    private val _showChecksum = MutableStateFlow(false)
    val showChecksum: StateFlow<Boolean> = _showChecksum.asStateFlow()

    private val _selectedDirectoryPath = MutableStateFlow("/storage/emulated/0")
    val selectedDirectoryPath: StateFlow<String> = _selectedDirectoryPath.asStateFlow()

    private val _selectedSourceType = MutableStateFlow(StorageType.INTERNAL)
    val selectedSourceType: StateFlow<StorageType> = _selectedSourceType.asStateFlow()

    private val _storageVolumes = MutableStateFlow<List<StorageVolumeInfo>>(emptyList())
    val storageVolumes: StateFlow<List<StorageVolumeInfo>> = _storageVolumes.asStateFlow()

    private val _userShortcuts = MutableStateFlow<List<FavoriteShortcut>>(emptyList())
    val userShortcuts: StateFlow<List<FavoriteShortcut>> = _userShortcuts.asStateFlow()

    // Room database integration
    private var repository: com.example.db.HistoricalReportRepository? = null

    private fun getRepository(context: Context): com.example.db.HistoricalReportRepository {
        return repository ?: synchronized(this) {
            val database = com.example.db.AppDatabase.getDatabase(context)
            val repo = com.example.db.HistoricalReportRepository(database.historicalReportDao())
            repository = repo
            repo
        }
    }

    private val _historicalReports = MutableStateFlow<List<com.example.db.HistoricalReport>>(emptyList())
    val historicalReports: StateFlow<List<com.example.db.HistoricalReport>> = _historicalReports.asStateFlow()

    fun loadHistoricalReports(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            getRepository(context).allReports.collect { list ->
                _historicalReports.value = list
            }
        }
    }

    fun deleteHistoricalReport(context: Context, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            getRepository(context).deleteById(id)
        }
    }

    // Help count files recursively
    private fun countFilesAndDirs(node: TreeReportGenerator.TreeDocNode): Pair<Int, Int> {
        var files = 0
        var dirs = 0
        fun traverse(n: TreeReportGenerator.TreeDocNode) {
            if (n.type == "directory") {
                dirs++
            } else {
                files++
            }
            n.children.forEach { traverse(it) }
        }
        node.children.forEach { traverse(it) }
        return Pair(dirs, files)
    }

    // Archive search state
    private val _archiveSearchQuery = MutableStateFlow("")
    val archiveSearchQuery: StateFlow<String> = _archiveSearchQuery.asStateFlow()

    private val _archiveSearchResults = MutableStateFlow<List<ArchiveSearchResultItem>>(emptyList())
    val archiveSearchResults: StateFlow<List<ArchiveSearchResultItem>> = _archiveSearchResults.asStateFlow()

    fun setArchiveSearchQuery(query: String) {
        _archiveSearchQuery.value = query
    }

    fun searchArchive(context: Context, query: String) {
        _archiveSearchQuery.value = query
        if (query.trim().isEmpty()) {
            _archiveSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val allReports = _historicalReports.value
            val matches = mutableListOf<ArchiveSearchResultItem>()
            val nameRegex = """\"name\":\s*\"([^\"]+)\"""".toRegex()
            val pathRegex = """\"path\":\s*\"([^\"]+)\"""".toRegex()
            
            val queryLower = query.lowercase().trim()
            allReports.forEach { report ->
                val lines = report.jsonContent.split("\n")
                var currentName = ""
                for (line in lines) {
                    val nameMatch = nameRegex.find(line)
                    if (nameMatch != null) {
                        currentName = nameMatch.groupValues[1]
                    }
                    val pathMatch = pathRegex.find(line)
                    if (pathMatch != null) {
                        val currentPath = pathMatch.groupValues[1]
                        if (currentName.lowercase().contains(queryLower) || currentPath.lowercase().contains(queryLower)) {
                            matches.add(
                                ArchiveSearchResultItem(
                                    reportName = report.outputName,
                                    reportId = report.id,
                                    timestamp = report.timestamp,
                                    fileName = currentName,
                                    path = currentPath
                                )
                            )
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                _archiveSearchResults.value = matches
            }
        }
    }

    // Comparison Engine variables and state
    private val _comparisonResult = MutableStateFlow<ReportComparisonResult?>(null)
    val comparisonResult: StateFlow<ReportComparisonResult?> = _comparisonResult.asStateFlow()

    fun clearComparisonResult() {
        _comparisonResult.value = null
    }

    fun compareHistoricalReports(context: Context, idA: Int, idB: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val repo = getRepository(context)
            val reportA = repo.getReportById(idA)
            val reportB = repo.getReportById(idB)
            
            if (reportA == null || reportB == null) return@launch
            
            val pathRegex = """\"path\":\s*\"([^\"]+)\"""".toRegex()
            
            val pathsA = mutableSetOf<String>()
            reportA.jsonContent.split("\n").forEach { line ->
                val match = pathRegex.find(line)
                if (match != null) {
                    pathsA.add(match.groupValues[1])
                }
            }

            val pathsB = mutableSetOf<String>()
            reportB.jsonContent.split("\n").forEach { line ->
                val match = pathRegex.find(line)
                if (match != null) {
                    pathsB.add(match.groupValues[1])
                }
            }

            val deleted = pathsA.subtract(pathsB).toList()
            val added = pathsB.subtract(pathsA).toList()

            withContext(Dispatchers.Main) {
                _comparisonResult.value = ReportComparisonResult(
                    reportAName = "${reportA.outputName} (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(reportA.timestamp))})",
                    reportBName = "${reportB.outputName} (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(reportB.timestamp))})",
                    foldersDiff = reportB.foldersCount - reportA.foldersCount,
                    filesDiff = reportB.filesCount - reportA.filesCount,
                    addedFiles = added,
                    deletedFiles = deleted
                )
            }
        }
    }

    // Operational and Loading States
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _scanProgress = MutableStateFlow("")
    val scanProgress: StateFlow<String> = _scanProgress.asStateFlow()

    private val _scannedCount = MutableStateFlow(0)
    val scannedCount: StateFlow<Int> = _scannedCount.asStateFlow()

    private val _generationSuccessUri = MutableStateFlow<Uri?>(null)
    val generationSuccessUri: StateFlow<Uri?> = _generationSuccessUri.asStateFlow()

    private val _generationSuccessPath = MutableStateFlow<String?>(null)
    val generationSuccessPath: StateFlow<String?> = _generationSuccessPath.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    fun selectDirectory(context: Context, uri: Uri) {
        _selectedDirectoryUri.value = uri
        
        // Resolve beautiful displaying name for directory
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val name = getDirectoryName(context, uri)
                _selectedDirectoryName.value = name
            } catch (e: Exception) {
                _selectedDirectoryName.value = uri.path?.substringAfterLast(':') ?: "مجلد غير معروف"
            }
        }
    }

    private fun getDirectoryName(context: Context, treeUri: Uri): String {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
        context.contentResolver.query(
            documentUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (idx >= 0) {
                    return cursor.getString(idx) ?: ""
                }
            }
        }
        return treeUri.path?.substringAfterLast(':') ?: "مجلد"
    }

    fun setFormat(value: String) { _format.value = value }
    fun setShowSize(value: Boolean) { _showSize.value = value }
    fun setShowMtime(value: Boolean) { _showMtime.value = value }
    fun setShowCount(value: Boolean) { _showCount.value = value }
    fun setMaxDepth(value: String) { _maxDepth.value = value }
    fun setExclude(value: String) { _exclude.value = value }
    fun setOutputName(value: String) { _outputName.value = value }
    fun setPathMode(value: String) { _pathMode.value = value }
    fun setShowCategory(value: Boolean) { _showCategory.value = value }
    fun setShowExtension(value: Boolean) { _showExtension.value = value }
    fun setShowModifiedIso(value: Boolean) { _showModifiedIso.value = value }
    fun setShowPermissions(value: Boolean) { _showPermissions.value = value }
    fun setShowMimeType(value: Boolean) { _showMimeType.value = value }
    fun setShowDepth(value: Boolean) { _showDepth.value = value }
    fun setShowChildrenCount(value: Boolean) { _showChildrenCount.value = value }
    fun setShowChecksum(value: Boolean) { _showChecksum.value = value }

    fun clearResultState() {
        _generationSuccessUri.value = null
        _generationSuccessPath.value = null
        _generationError.value = null
    }

    // load persistent user choices
    fun loadDefaults(context: Context) {
        val prefs = context.getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE)
        _format.value = prefs.getString("format", "html") ?: "html"
        _showSize.value = prefs.getBoolean("show_size", true)
        _showMtime.value = prefs.getBoolean("show_mtime", false)
        _showCount.value = prefs.getBoolean("show_count", false)
        _maxDepth.value = prefs.getString("max_depth", "") ?: ""
        _exclude.value = prefs.getString("exclude", ".git, __pycache__, node_modules") ?: ".git, __pycache__, node_modules"
        _outputName.value = prefs.getString("output_name", "tree_report") ?: "tree_report"
        _pathMode.value = prefs.getString("path_mode", "relative") ?: "relative"
        _showCategory.value = prefs.getBoolean("show_category", false)
        _showExtension.value = prefs.getBoolean("show_extension", false)
        _showModifiedIso.value = prefs.getBoolean("show_modified_iso", false)
        _showPermissions.value = prefs.getBoolean("show_permissions", false)
        _showMimeType.value = prefs.getBoolean("show_mime_type", false)
        _showDepth.value = prefs.getBoolean("show_depth", false)
        _showChildrenCount.value = prefs.getBoolean("show_children_count", false)
        _showChecksum.value = prefs.getBoolean("show_checksum", false)
        _selectedDirectoryPath.value = prefs.getString("selected_directory_path", "/storage/emulated/0") ?: "/storage/emulated/0"
        
        val savedType = prefs.getString("selected_source_type", "INTERNAL") ?: "INTERNAL"
        _selectedSourceType.value = try { StorageType.valueOf(savedType) } catch (e: Exception) { StorageType.INTERNAL }

        val shortcutSet = prefs.getStringSet("user_shortcuts", emptySet()) ?: emptySet()
        _userShortcuts.value = shortcutSet.map {
            val parts = it.split("|", limit = 2)
            FavoriteShortcut(parts[0], parts.getOrElse(1) { "" }, isDefault = false)
        }

        detectStorageVolumes(context)
        loadHistoricalReports(context)
    }

    // save persistent user choices
    fun saveDefaults(context: Context): Boolean {
        val prefs = context.getSharedPreferences("TreeDocPrefs", Context.MODE_PRIVATE)
        return prefs.edit().apply {
            putString("format", _format.value)
            putBoolean("show_size", _showSize.value)
            putBoolean("show_mtime", _showMtime.value)
            putBoolean("show_count", _showCount.value)
            putString("max_depth", _maxDepth.value)
            putString("exclude", _exclude.value)
            putString("output_name", _outputName.value)
            putString("path_mode", _pathMode.value)
            putBoolean("show_category", _showCategory.value)
            putBoolean("show_extension", _showExtension.value)
            putBoolean("show_modified_iso", _showModifiedIso.value)
            putBoolean("show_permissions", _showPermissions.value)
            putBoolean("show_mime_type", _showMimeType.value)
            putBoolean("show_depth", _showDepth.value)
            putBoolean("show_children_count", _showChildrenCount.value)
            putBoolean("show_checksum", _showChecksum.value)
            putString("selected_directory_path", _selectedDirectoryPath.value)
            putString("selected_source_type", _selectedSourceType.value.name)
            
            val shortcutSet = _userShortcuts.value.map { "${it.name}|${it.path}" }.toSet()
            putStringSet("user_shortcuts", shortcutSet)
        }.commit()
    }

    fun setDirectoryPath(path: String, context: Context) {
        _selectedDirectoryPath.value = path
        _selectedDirectoryName.value = path.substringAfterLast("/").ifEmpty { path }
        _selectedDirectoryUri.value = null
        saveDefaults(context)
    }

    fun setSelectedSourceType(type: StorageType, context: Context) {
        _selectedSourceType.value = type
        saveDefaults(context)
    }

    fun detectStorageVolumes(context: Context) {
        val list = mutableListOf<StorageVolumeInfo>()
        list.add(StorageVolumeInfo("التخزين الداخلي الكامل", "/storage/emulated/0", StorageType.INTERNAL))

        try {
            val storageDir = java.io.File("/storage")
            if (storageDir.exists() && storageDir.isDirectory) {
                storageDir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        val name = file.name
                        if (name != "self" && name != "emulated" && name != "sdcard0" && name != "knox") {
                            val path = file.absolutePath
                            if (name.lowercase().contains("usb") || name.lowercase().contains("otg")) {
                                list.add(StorageVolumeInfo("ذاكرة USB خارجية (OTG) (${name})", path, StorageType.USB_OTG))
                            } else {
                                list.add(StorageVolumeInfo("بطاقة SD الخارجية (${name})", path, StorageType.SD_CARD))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _storageVolumes.value = list
    }

    fun getAllShortcuts(context: Context): List<FavoriteShortcut> {
        val list = mutableListOf<FavoriteShortcut>()
        list.add(FavoriteShortcut("التخزين الداخلي الكامل", "/storage/emulated/0", isDefault = true))
        list.add(FavoriteShortcut("مجلد التحميلات (Downloads)", "/storage/emulated/0/Download", isDefault = true))
        list.add(FavoriteShortcut("مجلد المستندات (Documents)", "/storage/emulated/0/Documents", isDefault = true))
        
        _storageVolumes.value.forEach { volume ->
            if (volume.type == StorageType.SD_CARD) {
                list.add(FavoriteShortcut("بطاقة SD الخارجية (${volume.path.substringAfterLast("/")})", volume.path, isDefault = true))
            } else if (volume.type == StorageType.USB_OTG) {
                list.add(FavoriteShortcut("ذاكرة USB خارجية (OTG) (${volume.path.substringAfterLast("/")})", volume.path, isDefault = true))
            }
        }
        
        list.addAll(_userShortcuts.value)
        return list
    }

    fun saveUserShortcut(context: Context, name: String, path: String) {
        val newList = _userShortcuts.value.toMutableList()
        newList.removeAll { it.path == path }
        newList.add(FavoriteShortcut(name, path, isDefault = false))
        _userShortcuts.value = newList
        saveDefaults(context)
    }

    fun deleteUserShortcut(context: Context, shortcut: FavoriteShortcut) {
        val newList = _userShortcuts.value.toMutableList()
        newList.remove(shortcut)
        _userShortcuts.value = newList
        saveDefaults(context)
    }

    fun findDocumentInTree(context: Context, treeUri: Uri, parentDocId: String, displayName: String): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        try {
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (idCol >= 0 && nameCol >= 0) {
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameCol)
                        if (name != null && name.equals(displayName, ignoreCase = true)) {
                            val docId = cursor.getString(idCol)
                            return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore search limits
        }
        return null
    }

    fun generateReport(context: Context) {
        val treeUri = _selectedDirectoryUri.value
        val useFileApi = (treeUri == null)

        if (treeUri == null && _selectedDirectoryPath.value.isEmpty()) {
            _generationError.value = "الرجاء اختيار المجلد المستهدف أو تحديد المسار أولاً / Please select target path."
            return
        }

        _isGenerating.value = true
        _generationSuccessUri.value = null
        _generationSuccessPath.value = null
        _generationError.value = null
        _scanProgress.value = "جاري بدء فحص الملفات..."
        _scannedCount.value = 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Determine format
                val ext = when (_format.value) {
                    "html" -> "html"
                    "md" -> "md"
                    "json" -> "json"
                    "pdf" -> "pdf"
                    else -> "txt"
                }
                val cleanedOutputName = _outputName.value.trim().ifEmpty { "tree_report" }
                val targetFileName = "$cleanedOutputName.$ext"

                // Check depth limits
                var depthLimit: Int? = null
                val depthStr = _maxDepth.value.trim()
                if (depthStr.isNotEmpty()) {
                    try {
                        depthLimit = depthStr.toInt()
                    } catch (e: Exception) {
                        // Keep unlimited
                    }
                }

                // Prepare exclusions list
                val exPatterns = _exclude.value.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                val counter = java.util.concurrent.atomic.AtomicInteger(0)

                val rootNode: TreeDocNode?
                val rootName: String

                if (useFileApi) {
                    val rawPath = _selectedDirectoryPath.value
                    val file = java.io.File(rawPath)
                    if (!file.exists() || !file.isDirectory) {
                        throw Exception("المسار المحدد غير موجود أو ليس مجلداً صالحاً.")
                    }
                    rootName = file.name.ifEmpty { "المجلد المستهدف" }
                    rootNode = TreeReportGenerator.buildTreeFromFile(
                        file = file,
                        depth = 0,
                        maxDepth = depthLimit,
                        excludeList = exPatterns,
                        showSize = _showSize.value,
                        showMtime = _showMtime.value,
                        showCount = _showCount.value,
                        scannedCount = counter,
                        computeChecksum = _showChecksum.value,
                        onProgress = { currentFolder, totalSoFar ->
                            _scanProgress.value = "تحليل المجلد: $currentFolder"
                            _scannedCount.value = totalSoFar
                        }
                    )
                } else {
                    val rootDocId = DocumentsContract.getTreeDocumentId(treeUri!!)
                    rootName = _selectedDirectoryName.value ?: "المجلد المستهدف"
                    rootNode = TreeReportGenerator.buildTree(
                        context = context,
                        treeUri = treeUri,
                        docId = rootDocId,
                        name = rootName,
                        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                        sizeBytes = null,
                        lastModifiedMs = null,
                        depth = 0,
                        maxDepth = depthLimit,
                        excludeList = exPatterns,
                        showSize = _showSize.value,
                        showMtime = _showMtime.value,
                        showCount = _showCount.value,
                        scannedCount = counter,
                        computeChecksum = _showChecksum.value,
                        onProgress = { currentFolder, totalSoFar ->
                            _scanProgress.value = "تحليل المجلد: $currentFolder"
                            _scannedCount.value = totalSoFar
                        }
                    )
                }

                if (rootNode == null) {
                    withContext(Dispatchers.Main) {
                        _isGenerating.value = false
                        _generationError.value = "لم يتم العثور على أي ملفات أو مجلدات لتضمينها."
                    }
                    return@launch
                }

                val mimeType = when (_format.value) {
                    "html" -> "text/html"
                    "md" -> "text/markdown"
                    "json" -> "application/json"
                    "pdf" -> "application/pdf"
                    else -> "text/plain"
                }

                _scanProgress.value = "حفظ التقرير بصيغة ${ext.uppercase()}..."

                var resultUri: Uri? = null
                var resultPath: String? = null

                // Save report content
                if (useFileApi) {
                    val rawPath = _selectedDirectoryPath.value
                    val destFile = java.io.File(rawPath, targetFileName)
                    java.io.FileOutputStream(destFile).use { outStream ->
                        if (_format.value == "pdf") {
                            TreeReportGenerator.writePdf(
                                context = context,
                                node = rootNode,
                                outStream = outStream,
                                rootName = rootName,
                                showSize = _showSize.value,
                                showMtime = _showMtime.value,
                                showCount = _showCount.value
                            )
                        } else {
                            BufferedWriter(OutputStreamWriter(outStream, "UTF-8")).use { writer ->
                                when (_format.value) {
                                    "html" -> TreeReportGenerator.writeHtml(rootNode, writer, rootName, _showSize.value, _showMtime.value, _showCount.value)
                                    "md" -> TreeReportGenerator.writeMarkdown(rootNode, writer, rootName, _showSize.value, _showMtime.value, _showCount.value)
                                    "json" -> TreeReportGenerator.writeJson(
                                        node = rootNode,
                                        outWriter = writer,
                                        rootName = rootName,
                                        showSize = _showSize.value,
                                        showMtime = _showMtime.value,
                                        showCount = _showCount.value,
                                        pathMode = _pathMode.value,
                                        showCategory = _showCategory.value,
                                        showExtension = _showExtension.value,
                                        showModifiedIso = _showModifiedIso.value,
                                        showPermissions = _showPermissions.value,
                                        showMimeType = _showMimeType.value,
                                        showDepth = _showDepth.value,
                                        showChildrenCount = _showChildrenCount.value,
                                        showChecksum = _showChecksum.value
                                    )
                                    else -> TreeReportGenerator.writeTxt(rootNode, writer, rootName, _showSize.value, _showMtime.value, _showCount.value)
                                }
                            }
                        }
                    }
                    resultPath = destFile.absolutePath
                    resultUri = Uri.fromFile(destFile)
                } else {
                    val rootDocId = DocumentsContract.getTreeDocumentId(treeUri!!)
                    val rootDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootDocId)
                    var fileUri = findDocumentInTree(context, treeUri, rootDocId, targetFileName)
                    
                    if (fileUri == null) {
                        fileUri = DocumentsContract.createDocument(
                            context.contentResolver,
                            rootDocUri,
                            mimeType,
                            targetFileName
                        )
                    }

                    if (fileUri == null) {
                        throw Exception("فشل إنشاء ملف التقرير في المجلد المختار.")
                    }

                    context.contentResolver.openOutputStream(fileUri, "w")?.use { outStream ->
                        if (_format.value == "pdf") {
                            TreeReportGenerator.writePdf(
                                context = context,
                                node = rootNode,
                                outStream = outStream,
                                rootName = rootName,
                                showSize = _showSize.value,
                                showMtime = _showMtime.value,
                                showCount = _showCount.value
                            )
                        } else {
                            BufferedWriter(OutputStreamWriter(outStream, "UTF-8")).use { writer ->
                                when (_format.value) {
                                    "html" -> TreeReportGenerator.writeHtml(rootNode, writer, rootName, _showSize.value, _showMtime.value, _showCount.value)
                                    "md" -> TreeReportGenerator.writeMarkdown(rootNode, writer, rootName, _showSize.value, _showMtime.value, _showCount.value)
                                    "json" -> TreeReportGenerator.writeJson(
                                        node = rootNode,
                                        outWriter = writer,
                                        rootName = rootName,
                                        showSize = _showSize.value,
                                        showMtime = _showMtime.value,
                                        showCount = _showCount.value,
                                        pathMode = _pathMode.value,
                                        showCategory = _showCategory.value,
                                        showExtension = _showExtension.value,
                                        showModifiedIso = _showModifiedIso.value,
                                        showPermissions = _showPermissions.value,
                                        showMimeType = _showMimeType.value,
                                        showDepth = _showDepth.value,
                                        showChildrenCount = _showChildrenCount.value,
                                        showChecksum = _showChecksum.value
                                    )
                                    else -> TreeReportGenerator.writeTxt(rootNode, writer, rootName, _showSize.value, _showMtime.value, _showCount.value)
                                }
                            }
                        }
                    }
                    resultUri = fileUri
                    resultPath = targetFileName
                }

                // Save report content recursively to Historical Archive in Room db
                try {
                    val (dirs, files) = countFilesAndDirs(rootNode)
                    
                    val jsonWriter = java.io.StringWriter()
                    val bufferedJsonWriter = BufferedWriter(jsonWriter)
                    TreeReportGenerator.writeJson(
                        node = rootNode,
                        outWriter = bufferedJsonWriter,
                        rootName = rootName,
                        showSize = _showSize.value,
                        showMtime = _showMtime.value,
                        showCount = _showCount.value,
                        pathMode = _pathMode.value,
                        showCategory = _showCategory.value,
                        showExtension = _showExtension.value,
                        showModifiedIso = _showModifiedIso.value,
                        showPermissions = _showPermissions.value,
                        showMimeType = _showMimeType.value,
                        showDepth = _showDepth.value,
                        showChildrenCount = _showChildrenCount.value,
                        showChecksum = _showChecksum.value
                    )
                    bufferedJsonWriter.flush()
                    val jsonString = jsonWriter.toString()

                    val sizeTextDisplay = rootNode.size ?: "N/A"

                    val report = com.example.db.HistoricalReport(
                        rootPath = rootName,
                        outputName = cleanedOutputName,
                        foldersCount = dirs,
                        filesCount = files,
                        sizeText = sizeTextDisplay,
                        jsonContent = jsonString,
                        format = _format.value
                    )
                    getRepository(context).insert(report)
                } catch (dbEx: Exception) {
                    dbEx.printStackTrace()
                }

                withContext(Dispatchers.Main) {
                    _isGenerating.value = false
                    _generationSuccessUri.value = resultUri
                    _generationSuccessPath.value = resultPath
                    _scanProgress.value = ""
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isGenerating.value = false
                    _generationError.value = "حدث خطأ غير متوقع: ${e.localizedMessage ?: "مجهول"}"
                    _scanProgress.value = ""
                }
            }
        }
    }

    // Secondary utility actions: Clipboard, Open, Share
    fun copyReportToClipboard(context: Context, onCopied: () -> Unit) {
        val uri = _generationSuccessUri.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Read content back and copy
                val stringBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inStream ->
                    BufferedReader(InputStreamReader(inStream, "UTF-8")).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line).append("\n")
                        }
                    }
                }
                
                val textToCopy = stringBuilder.toString()
                withContext(Dispatchers.Main) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("TreeDoc Report", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    onCopied()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Fallback clipboard message
                }
            }
        }
    }

    fun openReport(context: Context) {
        val uri = _generationSuccessUri.value ?: return
        val ext = when (_format.value) {
            "html" -> "html"
            "md" -> "md"
            "json" -> "json"
            "pdf" -> "pdf"
            else -> "txt"
        }
        val mimeType = when (_format.value) {
            "html" -> "text/html"
            "md" -> "text/markdown"
            "json" -> "application/json"
            "pdf" -> "application/pdf"
            else -> "text/plain"
        }
        
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "عرض تقرير الشجرة ($ext)"))
        } catch (e: Exception) {
            // Context fallback
        }
    }

    fun shareReport(context: Context) {
        val uri = _generationSuccessUri.value ?: return
        val ext = when (_format.value) {
            "html" -> "html"
            "md" -> "md"
            "json" -> "json"
            "pdf" -> "pdf"
            else -> "txt"
        }
        val mimeType = when (_format.value) {
            "html" -> "text/html"
            "md" -> "text/markdown"
            "json" -> "application/json"
            "pdf" -> "application/pdf"
            else -> "text/plain"
        }

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "المعمار الشجري - د. محمد المحطوري")
                putExtra(Intent.EXTRA_TEXT, "تم توليد تقرير المعمار الشجري بنجاح.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة تقرير الشجرة ($ext)"))
        } catch (e: Exception) {
            // Share fail fallback
        }
    }
}

data class ArchiveSearchResultItem(
    val reportName: String,
    val reportId: Int,
    val timestamp: Long,
    val fileName: String,
    val path: String
)

data class ReportComparisonResult(
    val reportAName: String,
    val reportBName: String,
    val foldersDiff: Int,
    val filesDiff: Int,
    val addedFiles: List<String>,   // Present in B but not in A
    val deletedFiles: List<String>  // Present in A but not in B
)

enum class StorageType {
    INTERNAL, SD_CARD, USB_OTG, CUSTOM
}

data class StorageVolumeInfo(
    val name: String,
    val path: String,
    val type: StorageType
)

data class FavoriteShortcut(
    val name: String,
    val path: String,
    val isDefault: Boolean = false
)

