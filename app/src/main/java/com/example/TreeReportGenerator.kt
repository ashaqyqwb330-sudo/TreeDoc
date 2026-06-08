package com.example

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TreeReportGenerator {

    data class TreeDocNode(
        val name: String,
        val docId: String,
        val type: String, // "directory" or "file"
        var size: String? = null,
        var mtime: String? = null,
        var count: Int? = null,
        var absolutePath: String = "",
        var relativePath: String = "",
        var category: String = "",
        val children: MutableList<TreeDocNode> = mutableListOf(),
        var extension: String? = null,
        var modifiedIso: String? = null,
        var permissions: String? = null,
        var mimeType: String? = null,
        var depth: Int = 0,
        var checksum: String? = null
    )

    fun getAbsolutePathFromDocId(docId: String): String {
        if (docId.isEmpty()) return ""
        return if (docId.contains(":")) {
            val parts = docId.split(":", limit = 2)
            val volume = parts[0]
            val path = parts[1]
            if (volume == "primary") {
                "/storage/emulated/0/$path"
            } else {
                "/storage/$volume/$path"
            }
        } else {
            "/storage/emulated/0/$docId"
        }
    }

    fun getFileCategory(name: String, isDir: Boolean): String {
        if (isDir) return "مجلد"
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext.isEmpty()) return "غير معروف"
        return when (ext) {
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> "صورة"
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "html", "htm", "md", "epub", "json" -> "مستند"
            "mp4", "mkv", "avi", "mov", "flv", "wmv", "mpeg", "webm" -> "فيديو"
            "mp3", "wav", "ogg", "m4a", "flac", "aac" -> "صوت"
            "zip", "rar", "tar", "gz", "7z" -> "ملف مضغوط"
            "kt", "java", "cpp", "c", "py", "js", "ts", "css", "xml", "sh" -> "برمجيات/أكواد"
            "apk", "exe", "dmg", "bin" -> "تطبيق/نظام"
            else -> "غير معروف"
        }
    }

    fun getReadableSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        if (sizeBytes < 1024) return "$sizeBytes B"
        var value = sizeBytes.toDouble()
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 0
        while (value >= 1024 && i < 5) {
            value /= 1024
            ci.next()
            i++
        }
        return String.format(Locale.US, "%.1f %cB", value, ci.current())
    }

    fun getReadableTime(timeMs: Long): String {
        if (timeMs <= 0) return "غير متاح"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timeMs))
    }

    // High performance queries direct via cursor traversal
    fun buildTree(
        context: Context,
        treeUri: Uri,
        docId: String,
        name: String,
        mimeType: String,
        sizeBytes: Long?,
        lastModifiedMs: Long?,
        depth: Int,
        maxDepth: Int?,
        excludeList: List<String>,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean,
        scannedCount: java.util.concurrent.atomic.AtomicInteger,
        parentRelativePath: String = "",
        parentAbsolutePath: String = "",
        computeChecksum: Boolean = false,
        onProgress: (String, Int) -> Unit
    ): TreeDocNode? {
        val isDir = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
        val typeStr = if (isDir) "directory" else "file"
        val displayName = if (name.isEmpty()) "المجلد الجذر" else name

        // Check if any exclusion pattern matches
        if (excludeList.isNotEmpty() && excludeList.any { pattern -> displayName.contains(pattern, ignoreCase = true) }) {
            return null
        }

        val totalScanned = scannedCount.incrementAndGet()

        val nodeRelativePath = if (parentRelativePath.isEmpty()) {
            displayName
        } else {
            "$parentRelativePath/$displayName"
        }

        val nodeAbsolutePath = if (parentAbsolutePath.isEmpty()) {
            getAbsolutePathFromDocId(docId)
        } else {
            if (parentAbsolutePath.endsWith("/")) {
                parentAbsolutePath + displayName
            } else {
                "$parentAbsolutePath/$displayName"
            }
        }

        val nodeCategory = getFileCategory(displayName, isDir)

        // Compute additional metadata properties
        val ext = if (isDir) null else {
            val idx = displayName.lastIndexOf('.')
            if (idx > 0 && idx < displayName.length - 1) {
                displayName.substring(idx + 1).lowercase()
            } else {
                ""
            }
        }

        val modifiedIso = if (lastModifiedMs != null && lastModifiedMs > 0) {
            try {
                val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                sdfIso.format(Date(lastModifiedMs))
            } catch (e: Exception) {
                null
            }
        } else null

        val perms = if (isDir) "rwx" else "rw-"

        var chksum: String? = null
        if (computeChecksum && !isDir) {
            try {
                val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                if (sizeBytes == null || sizeBytes < 10 * 1024 * 1024) { // limit to 10MB to avoid absolute UI freeze
                    context.contentResolver.openInputStream(fileUri)?.use { input ->
                        val digest = java.security.MessageDigest.getInstance("MD5")
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            digest.update(buffer, 0, read)
                        }
                        val md5sum = digest.digest()
                        val bigInt = java.math.BigInteger(1, md5sum)
                        var hex = bigInt.toString(16)
                        while (hex.length < 32) {
                            hex = "0$hex"
                        }
                        chksum = hex
                    }
                } else {
                    chksum = "skipped_large_file"
                }
            } catch (e: Exception) {
                chksum = "error_reading"
            }
        }

        val node = TreeDocNode(
            name = displayName,
            docId = docId,
            type = typeStr,
            absolutePath = nodeAbsolutePath,
            relativePath = nodeRelativePath,
            category = nodeCategory,
            extension = ext,
            modifiedIso = modifiedIso,
            permissions = perms,
            mimeType = mimeType,
            depth = depth,
            checksum = chksum
        )

        if (!isDir) {
            if (showSize && sizeBytes != null) {
                node.size = getReadableSize(sizeBytes)
            }
            if (showMtime && lastModifiedMs != null && lastModifiedMs > 0) {
                node.mtime = getReadableTime(lastModifiedMs)
            }
        } else {
            if (showMtime && lastModifiedMs != null && lastModifiedMs > 0) {
                node.mtime = getReadableTime(lastModifiedMs)
            }

            // Recurse children if limit isn't reached
            if (maxDepth == null || depth < maxDepth) {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
                try {
                    onProgress(displayName, totalScanned)
                    context.contentResolver.query(
                        childrenUri,
                        arrayOf(
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_MIME_TYPE,
                            DocumentsContract.Document.COLUMN_SIZE,
                            DocumentsContract.Document.COLUMN_LAST_MODIFIED
                        ),
                        null, null, null
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        val mimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                        val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                        val mtimeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                        val dirList = mutableListOf<TreeDocNode>()
                        val fileList = mutableListOf<TreeDocNode>()

                        while (cursor.moveToNext()) {
                            val childDocId = cursor.getString(idCol)
                            val childName = cursor.getString(nameCol) ?: "unnamed"
                            val childMime = cursor.getString(mimeCol) ?: "file/unknown"
                            val childSize = if (cursor.isNull(sizeCol)) null else cursor.getLong(sizeCol)
                            val childMtime = if (cursor.isNull(mtimeCol)) null else cursor.getLong(mtimeCol)

                            // Skip exclusions early
                            if (excludeList.isNotEmpty() && excludeList.any { pattern -> childName.contains(pattern, ignoreCase = true) }) {
                                continue
                            }

                            val childNode = buildTree(
                                context = context,
                                treeUri = treeUri,
                                childDocId,
                                childName,
                                childMime,
                                childSize,
                                childMtime,
                                depth + 1,
                                maxDepth,
                                excludeList,
                                showSize,
                                showMtime,
                                showCount,
                                scannedCount,
                                nodeRelativePath,
                                nodeAbsolutePath,
                                computeChecksum,
                                onProgress
                            )
                            if (childNode != null) {
                                if (childNode.type == "directory") {
                                    dirList.add(childNode)
                                } else {
                                    fileList.add(childNode)
                                }
                            }
                        }

                        // Order directories first, then alphabetical files
                        dirList.sortBy { it.name.lowercase() }
                        fileList.sortBy { it.name.lowercase() }

                        node.children.addAll(dirList)
                        node.children.addAll(fileList)

                        if (showCount) {
                            node.count = node.children.size
                        }
                    }
                } catch (e: Exception) {
                    node.children.add(
                        TreeDocNode(
                            name = "❗ لا يمكن الوصول - تفاصيل الخطأ: ${e.localizedMessage ?: "مجهول"}",
                            docId = "",
                            type = "error"
                        )
                    )
                }
            }
    fun buildTreeFromFile(
        file: java.io.File,
        depth: Int,
        maxDepth: Int?,
        excludeList: List<String>,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean,
        scannedCount: java.util.concurrent.atomic.AtomicInteger,
        parentRelativePath: String = "",
        computeChecksum: Boolean = false,
        onProgress: (String, Int) -> Unit
    ): TreeDocNode? {
        val isDir = file.isDirectory
        val typeStr = if (isDir) "directory" else "file"
        val displayName = file.name.ifEmpty { if (isDir) "المجلد الجذر" else "ملف" }

        if (excludeList.isNotEmpty() && excludeList.any { pattern -> displayName.contains(pattern, ignoreCase = true) }) {
            return null
        }

        val totalScanned = scannedCount.incrementAndGet()

        val nodeRelativePath = if (parentRelativePath.isEmpty()) {
            displayName
        } else {
            "$parentRelativePath/$displayName"
        }

        val nodeAbsolutePath = file.absolutePath
        val nodeCategory = getFileCategory(displayName, isDir)

        // Compute additional metadata properties
        val ext = if (isDir) null else {
            val idx = displayName.lastIndexOf('.')
            if (idx > 0 && idx < displayName.length - 1) {
                displayName.substring(idx + 1).lowercase()
            } else {
                ""
            }
        }

        val lastModifiedMs = file.lastModified()
        val modifiedIso = if (lastModifiedMs > 0) {
            try {
                val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                sdfIso.format(Date(lastModifiedMs))
            } catch (e: Exception) {
                null
            }
        } else null

        val r = if (file.canRead()) "r" else "-"
        val w = if (file.canWrite()) "w" else "-"
        val x = if (isDir) "x" else "-"
        val perms = "$r$w$x"

        var chksum: String? = null
        if (computeChecksum && !isDir) {
            try {
                val len = file.length()
                if (len < 10 * 1024 * 1024) { // limit 10MB
                    val digest = java.security.MessageDigest.getInstance("MD5")
                    java.io.FileInputStream(file).use { input ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            digest.update(buffer, 0, read)
                        }
                    }
                    val md5sum = digest.digest()
                    val bigInt = java.math.BigInteger(1, md5sum)
                    var hex = bigInt.toString(16)
                    while (hex.length < 32) {
                        hex = "0$hex"
                    }
                    chksum = hex
                } else {
                    chksum = "skipped_large_file"
                }
            } catch (e: Exception) {
                chksum = "error_reading"
            }
        }

        // Guess MIME type
        val mimeType = if (isDir) {
            "vnd.android.document/directory"
        } else {
            val extension = displayName.substringAfterLast('.', "")
            android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
        }

        val node = TreeDocNode(
            name = displayName,
            docId = file.absolutePath,
            type = typeStr,
            absolutePath = nodeAbsolutePath,
            relativePath = nodeRelativePath,
            category = nodeCategory,
            extension = ext,
            modifiedIso = modifiedIso,
            permissions = perms,
            mimeType = mimeType,
            depth = depth,
            checksum = chksum
        )

        if (!isDir) {
            if (showSize) {
                node.size = getReadableSize(file.length())
            }
            if (showMtime && lastModifiedMs > 0) {
                node.mtime = getReadableTime(lastModifiedMs)
            }
        } else {
            if (showMtime && lastModifiedMs > 0) {
                node.mtime = getReadableTime(lastModifiedMs)
            }

            if (maxDepth == null || depth < maxDepth) {
                try {
                    onProgress(displayName, totalScanned)
                    val childrenFiles = file.listFiles()
                    if (childrenFiles != null) {
                        val dirList = mutableListOf<TreeDocNode>()
                        val fileList = mutableListOf<TreeDocNode>()

                        for (child in childrenFiles) {
                            val childName = child.name
                            if (excludeList.isNotEmpty() && excludeList.any { pattern -> childName.contains(pattern, ignoreCase = true) }) {
                                continue
                            }

                            val childNode = buildTreeFromFile(
                                file = child,
                                depth = depth + 1,
                                maxDepth = maxDepth,
                                excludeList = excludeList,
                                showSize = showSize,
                                showMtime = showMtime,
                                showCount = showCount,
                                scannedCount = scannedCount,
                                parentRelativePath = nodeRelativePath,
                                computeChecksum = computeChecksum,
                                onProgress = onProgress
                            )
                            if (childNode != null) {
                                if (childNode.type == "directory") {
                                    dirList.add(childNode)
                                } else {
                                    fileList.add(childNode)
                                }
                            }
                        }

                        dirList.sortBy { it.name.lowercase() }
                        fileList.sortBy { it.name.lowercase() }

                        node.children.addAll(dirList)
                        node.children.addAll(fileList)

                        if (showCount) {
                            node.count = node.children.size
                        }
                    }
                } catch (e: Exception) {
                    node.children.add(
                        TreeDocNode(
                            name = "❗ لا يمكن الوصول - تفاصيل الخطأ: ${e.localizedMessage ?: "مجهول"}",
                            docId = "",
                            type = "error"
                        )
                    )
                }
            }
        }
        return node
    }

    fun generateTextLines(
        nodes: List<TreeDocNode>,
        prefix: String = "",
        showSize: Boolean = false,
        showMtime: Boolean = false,
        showCount: Boolean = false
    ): List<String> {
        val lines = mutableListOf<String>()
        val size = nodes.size
        nodes.forEachIndexed { index, node ->
            val isLastItem = index == size - 1
            val connector = if (isLastItem) "└── " else "├── "
            var line = "$prefix$connector${node.name}"
            val extras = mutableListOf<String>()
            if (showSize && !node.size.isNullOrEmpty()) {
                extras.add("[${node.size}]")
            }
            if (showMtime && !node.mtime.isNullOrEmpty()) {
                extras.add("(${node.mtime})")
            }
            if (showCount && node.count != null && node.type == "directory") {
                extras.add("📁 ${node.count} عناصر")
            }
            if (extras.isNotEmpty()) {
                line += " " + extras.joinToString(" ")
            }
            lines.add(line)

            if (node.children.isNotEmpty()) {
                val newPrefix = prefix + if (isLastItem) "    " else "│   "
                lines.addAll(
                    generateTextLines(
                        node.children,
                        newPrefix,
                        showSize,
                        showMtime,
                        showCount
                    )
                )
            }
        }
        return lines
    }

    // Write structure to output stream
    fun writeTxt(
        node: TreeDocNode,
        outWriter: BufferedWriter,
        rootName: String,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        outWriter.write("تقرير هيكل المجلدات - TreeDoc\n")
        outWriter.write("التاريخ: $dateStr\n")
        outWriter.write("المسار المستهدف: $rootName\n")
        outWriter.write("=".repeat(60) + "\n\n")

        // Write root node line
        val rootExtras = mutableListOf<String>()
        if (showMtime && !node.mtime.isNullOrEmpty()) rootExtras.add("(${node.mtime})")
        if (showCount && node.count != null) rootExtras.add("📁 ${node.count} عناصر")
        val rootSuffix = if (rootExtras.isNotEmpty()) " " + rootExtras.joinToString(" ") else ""
        outWriter.write("${node.name}$rootSuffix\n")

        val lines = generateTextLines(node.children, "", showSize, showMtime, showCount)
        for (line in lines) {
            outWriter.write(line + "\n")
        }
    }

    fun writeMarkdown(
        node: TreeDocNode,
        outWriter: BufferedWriter,
        rootName: String,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        outWriter.write("# هيكل المجلدات: `$rootName`\n\n")
        outWriter.write("**تاريخ التقرير:** $dateStr  \n")
        outWriter.write("**اسم التطبيق:** TreeDoc 🌳  \n\n")
        outWriter.write("```text\n")

        val rootExtras = mutableListOf<String>()
        if (showMtime && !node.mtime.isNullOrEmpty()) rootExtras.add("(${node.mtime})")
        if (showCount && node.count != null) rootExtras.add("📁 ${node.count} عناصر")
        val rootSuffix = if (rootExtras.isNotEmpty()) " " + rootExtras.joinToString(" ") else ""
        outWriter.write("${node.name}$rootSuffix\n")

        val lines = generateTextLines(node.children, "", showSize, showMtime, showCount)
        for (line in lines) {
            outWriter.write(line + "\n")
        }
        outWriter.write("```\n")
    }

    fun writeJson(
        node: TreeDocNode,
        outWriter: BufferedWriter,
        rootName: String,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean,
        pathMode: String = "relative",
        showCategory: Boolean = false,
        showExtension: Boolean = false,
        showModifiedIso: Boolean = false,
        showPermissions: Boolean = false,
        showMimeType: Boolean = false,
        showDepth: Boolean = false,
        showChildrenCount: Boolean = false,
        showChecksum: Boolean = false
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        outWriter.write("{\n")
        outWriter.write("  \"metadata\": {\n")
        outWriter.write("    \"root_path\": \"${escapeJson(rootName)}\",\n")
        outWriter.write("    \"path_mode\": \"$pathMode\",\n")
        outWriter.write("    \"generated_at\": \"$dateStr\",\n")
        outWriter.write("    \"generator\": \"TreeDoc for Android\"\n")
        outWriter.write("  },\n")
        outWriter.write("  \"tree\": ")
        writeJsonNode(
            node = node,
            outWriter = outWriter,
            indent = "  ",
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
        outWriter.write("\n}\n")
    }

    private fun writeJsonNode(
        node: TreeDocNode,
        outWriter: BufferedWriter,
        indent: String,
        pathMode: String = "relative",
        showCategory: Boolean = false,
        showExtension: Boolean = false,
        showModifiedIso: Boolean = false,
        showPermissions: Boolean = false,
        showMimeType: Boolean = false,
        showDepth: Boolean = false,
        showChildrenCount: Boolean = false,
        showChecksum: Boolean = false
    ) {
        outWriter.write("{\n")
        outWriter.write("$indent  \"name\": \"${escapeJson(node.name)}\",\n")
        outWriter.write("$indent  \"type\": \"${node.type}\"")
        
        val p = if (pathMode == "absolute") node.absolutePath else node.relativePath
        outWriter.write(",\n$indent  \"path\": \"${escapeJson(p)}\"")
        
        if (showCategory) {
            outWriter.write(",\n$indent  \"category\": \"${escapeJson(node.category)}\"")
        }

        if (showExtension && node.extension != null) {
            outWriter.write(",\n$indent  \"extension\": \"${escapeJson(node.extension!!)}\"")
        }

        if (showModifiedIso && node.modifiedIso != null) {
            outWriter.write(",\n$indent  \"modified\": \"${escapeJson(node.modifiedIso!!)}\"")
        }

        if (showPermissions && node.permissions != null) {
            outWriter.write(",\n$indent  \"permissions\": \"${escapeJson(node.permissions!!)}\"")
        }

        if (showMimeType && node.mimeType != null) {
            outWriter.write(",\n$indent  \"mime_type\": \"${escapeJson(node.mimeType!!)}\"")
        }

        if (showDepth) {
            outWriter.write(",\n$indent  \"depth\": ${node.depth}")
        }

        if (showChildrenCount && node.type == "directory") {
            outWriter.write(",\n$indent  \"children_count\": ${node.children.size}")
        }

        if (showChecksum && node.checksum != null) {
            outWriter.write(",\n$indent  \"checksum\": \"${escapeJson(node.checksum!!)}\"")
        }

        if (node.size != null) {
            outWriter.write(",\n$indent  \"size\": \"${escapeJson(node.size!!)}\"")
        }
        if (node.mtime != null) {
            outWriter.write(",\n$indent  \"mtime\": \"${escapeJson(node.mtime!!)}\"")
        }
        if (node.count != null) {
            outWriter.write(",\n$indent  \"count\": ${node.count}")
        }
        if (node.children.isNotEmpty()) {
            outWriter.write(",\n$indent  \"children\": [\n")
            node.children.forEachIndexed { i, child ->
                outWriter.write("$indent    ")
                writeJsonNode(
                    node = child,
                    outWriter = outWriter,
                    indent = "$indent    ",
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
                if (i < node.children.size - 1) {
                    outWriter.write(",\n")
                } else {
                    outWriter.write("\n")
                }
            }
            outWriter.write("$indent  ]")
        } else {
            outWriter.write(",\n$indent  \"children\": []")
        }
        outWriter.write("\n$indent}")
    }

    fun writeHtml(
        node: TreeDocNode,
        outWriter: BufferedWriter,
        rootName: String,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Escape inputs
        val escRootName = escapeHtml(rootName)

        outWriter.write("""<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>المعمار الشجري - د. محمد المحطوري - $escRootName</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Amiri:ital,wght@0,400;0,700;1,400&family=Tajawal:wght@300;400;500;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-color: #0b0f19;
            --container-bg: #131b2e;
            --sidebar-bg: #1c2541;
            --text-color: #f1f5f9;
            --text-muted: #94a3b8;
            --primary-color: #10b981;
            --hover-bg: #1e293b;
            --accent-color: #3b82f6;
            --border-color: #334155;
            --card-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 8px 10px -6px rgba(0, 0, 0, 0.4);
            --header-gradient: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
        }
        
        body {
            font-family: 'Tajawal', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            margin: 0;
            padding: 24px;
            line-height: 1.6;
        }

        .main-wrapper {
            max-width: 1200px;
            margin: 0 auto;
            display: grid;
            grid-template-columns: 350px 1fr;
            gap: 24px;
        }

        @media (max-width: 900px) {
            .main-wrapper {
                grid-template-columns: 1fr;
            }
        }

        /* Sidebar info style */
        .sidebar {
            background-color: var(--sidebar-bg);
            border-radius: 16px;
            padding: 24px;
            border: 1px solid var(--border-color);
            box-shadow: var(--card-shadow);
            height: fit-content;
        }

        .sidebar-logo {
            text-align: center;
            padding-bottom: 20px;
            border-bottom: 1px dashed var(--border-color);
            margin-bottom: 20px;
        }

        .sidebar-logo h2 {
            color: var(--primary-color);
            font-size: 1.4rem;
            margin: 0 0 8px 0;
            font-weight: 800;
        }

        .sidebar-logo p {
            font-size: 0.85rem;
            color: var(--text-muted);
            margin: 0;
        }

        .info-group {
            margin-bottom: 16px;
        }

        .info-label {
            font-size: 0.8rem;
            color: var(--text-muted);
            font-weight: bold;
            display: block;
            margin-bottom: 4px;
        }

        .info-val {
            font-size: 0.95rem;
            color: var(--text-color);
            background: rgba(15, 23, 42, 0.3);
            padding: 8px 12px;
            border-radius: 8px;
            border: 1.5px solid rgba(255, 255, 255, 0.03);
            word-break: break-all;
            display: block;
        }

        /* Stats indicators counters */
        .stats-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            margin-top: 20px;
        }

        .stat-card {
            background: rgba(15, 23, 42, 0.4);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 12px;
            text-align: center;
        }

        .stat-num {
            font-size: 1.3rem;
            font-weight: bold;
            color: var(--primary-color);
            display: block;
        }

        .stat-lbl {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        /* Workspace report section */
        .report-content {
            background-color: var(--container-bg);
            border-radius: 16px;
            padding: 24px;
            box-shadow: var(--card-shadow);
            border: 1px solid var(--border-color);
        }

        h1 {
            color: #ffffff;
            margin-top: 0;
            font-size: 1.8rem;
            font-weight: 800;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .search-container {
            position: relative;
            margin-bottom: 24px;
        }

        .search-container input {
            width: 100%;
            padding: 12px 18px;
            border-radius: 12px;
            border: 1.5px solid var(--border-color);
            background-color: var(--bg-color);
            color: var(--text-color);
            font-size: 0.95rem;
            outline: none;
            transition: all 0.3s;
            box-sizing: border-box;
        }

        .search-container input:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 10px rgba(16, 185, 129, 0.2);
        }

        .controls-toolbar {
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
        }

        .btn-group {
            display: flex;
            gap: 8px;
        }

        .btn {
            background-color: var(--hover-bg);
            color: var(--text-color);
            border: 1px solid var(--border-color);
            border-radius: 10px;
            padding: 8px 16px;
            font-size: 0.85rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .btn:hover {
            background-color: var(--primary-color);
            border-color: var(--primary-color);
            color: #0b0f19;
            font-weight: bold;
        }

        .btn-accent:hover {
            background-color: var(--accent-color);
            border-color: var(--accent-color);
            color: white;
        }

        /* Tree lists styles */
        ul {
            list-style: none;
            padding-right: 24px;
            margin: 4px 0;
            border-right: 1.5px dashed var(--border-color);
        }

        .tree-container-root > ul {
            padding-right: 0;
            border-right: none;
        }

        li {
            margin: 6px 0;
            position: relative;
        }

        details {
            margin: 4px 0;
        }

        summary {
            list-style: none;
            outline: none;
            cursor: pointer;
            padding: 8px 12px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: background-color 0.15s;
        }

        summary::-webkit-details-marker {
            display: none;
        }

        summary:hover {
            background-color: var(--hover-bg);
        }

        .folder-summary::before {
            content: "◀";
            font-size: 0.75rem;
            color: var(--primary-color);
            transition: transform 0.2s ease-in-out;
            display: inline-block;
        }

        details[open] > summary.folder-summary::before {
            transform: rotate(-90deg);
        }

        .icon {
            font-size: 1.2rem;
            width: 24px;
            display: inline-block;
            text-align: center;
        }

        .file-item {
            padding: 8px 16px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: background-color 0.15s;
        }

        .file-item:hover {
            background-color: rgba(30, 41, 59, 0.7);
        }

        /* Metadata Badges inside report */
        .meta {
            font-size: 0.75rem;
            color: var(--text-muted);
            background-color: rgba(15, 23, 42, 0.5);
            padding: 2px 8px;
            border-radius: 6px;
            margin-right: auto;
            border: 1.5px solid rgba(255, 255, 255, 0.03);
            display: inline-flex;
            gap: 8px;
        }

        .badge {
            font-size: 0.7rem;
            background-color: rgba(59, 130, 246, 0.1);
            color: var(--accent-color);
            padding: 2px 6px;
            border-radius: 4px;
            border: 1px solid rgba(59, 130, 246, 0.3);
        }

        .badge-size {
            color: #10b981;
            background-color: rgba(16, 185, 129, 0.1);
            border-color: rgba(16, 185, 129, 0.3);
        }

        .badge-time {
            color: #f43f5e;
            background-color: rgba(244, 63, 94, 0.1);
            border-color: rgba(244, 63, 94, 0.3);
        }

        .hidden {
            display: none !important;
        }

        .highlight {
            color: #0b0f19;
            font-weight: bold;
            background-color: #fbbf24;
            padding: 0 4px;
            border-radius: 4px;
            box-shadow: 0 0 8px rgba(251, 191, 36, 0.6);
        }

        /* Tab Styles */
        .tabs-header {
            display: flex;
            gap: 16px;
            border-bottom: 2px solid var(--border-color);
            margin-bottom: 24px;
            padding-bottom: 4px;
        }

        .tab-btn {
            background: none;
            border: none;
            color: var(--text-muted);
            font-size: 1rem;
            font-weight: bold;
            padding: 8px 16px;
            cursor: pointer;
            transition: all 0.3s;
            position: relative;
            font-family: 'Tajawal', sans-serif;
            outline: none;
        }

        .tab-btn.active {
            color: var(--primary-color);
        }

        .tab-btn.active::after {
            content: '';
            position: absolute;
            bottom: -6px;
            left: 0;
            right: 0;
            height: 3px;
            background-color: var(--primary-color);
            border-radius: 2px;
        }

        .tab-content {
            display: none;
            animation: fadeIn 0.25s ease-out;
        }

        .tab-content.active {
            display: block;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(4px); }
            to { opacity: 1; transform: translateY(0); }
        }

        /* Smart Categorization Cards */
        .category-section {
            background: rgba(15, 23, 42, 0.4);
            border: 1.5px solid var(--border-color);
            border-radius: 12px;
            margin-bottom: 14px;
            overflow: hidden;
            transition: border-color 0.2s;
        }

        .category-section:hover {
            border-color: rgba(16, 185, 129, 0.3);
        }

        .category-header {
            background-color: var(--sidebar-bg);
            padding: 12px 18px;
            font-weight: bold;
            display: flex;
            justify-content: space-between;
            align-items: center;
            cursor: pointer;
            user-select: none;
        }

        .category-header:hover {
            background-color: var(--hover-bg);
        }

        .category-body {
            padding: 12px 18px;
            background: rgba(15, 23, 42, 0.2);
            border-top: 1px solid var(--border-color);
        }

        .category-file-card {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 12px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.03);
            font-size: 0.9rem;
            gap: 12px;
        }

        .category-file-card:last-child {
            border-bottom: none;
        }

        .cat-file-name {
            font-weight: 500;
            color: var(--text-color);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .cat-file-path {
            font-size: 0.75rem;
            color: var(--text-muted);
            direction: ltr;
            text-align: right;
            word-break: break-all;
            background: rgba(255, 255, 255, 0.02);
            padding: 2px 6px;
            border-radius: 4px;
            max-width: 50%;
        }

        footer {
            margin-top: 40px;
            text-align: center;
            font-size: 0.8rem;
            color: var(--text-muted);
            border-top: 1px solid var(--border-color);
            padding-top: 20px;
        }
        
        .raw-tree-container {
            display: none;
            background-color: #05070e;
            border: 1.5px solid var(--border-color);
            padding: 16px;
            border-radius: 12px;
            font-family: 'Courier New', Courier, monospace;
            font-size: 0.85rem;
            color: #34d399;
            white-space: pre;
            overflow-x: auto;
            margin-top: 16px;
        }
    </style>
</head>
<body>

<div class="main-wrapper">
    <!-- Sidebar Information -->
    <div class="sidebar">
        <div class="sidebar-logo">
            <h2>المعمار الشجري</h2>
            <p>د. محمد هاشم المحطوري</p>
        </div>
        
        <div class="info-group" style="margin-top: 15px;">
            <span class="info-label">📂 مجلد المصدر الرئيسي:</span>
            <span class="info-val">$escRootName</span>
        </div>
        
        <div class="info-group">
            <span class="info-label">🕒 تاريخ استخراج التقرير:</span>
            <span class="info-val">$dateStr</span>
        </div>
        
        <div class="info-group">
            <span class="info-label">🛠️ تقنية الاستخراج:</span>
            <span class="info-val">مستكشف المجلدات الذكي للأندرويد</span>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <span id="folders-count" class="stat-num">-</span>
                <span class="stat-lbl">مجلدات فرعية</span>
            </div>
            <div class="stat-card">
                <span id="files-count" class="stat-num">-</span>
                <span class="stat-lbl">مستندات وملفات</span>
            </div>
        </div>

        <div style="background-color: rgba(16, 185, 129, 0.05); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: 12px; padding: 14px; margin-top: 24px; font-size: 0.8rem; color: #a7f3d0; text-align: justify;">
            <strong>💡 تصفح تفاعلي بالكامل:</strong> يمكنك فتح وإغلاق المجلدات الفرعية بالنقر المباشر، كما يمكنك كتابة أي ملف في شريط البحث للوصول الفوري وتوسيع هيكل شجرة المسار تلقائيًا.
        </div>
    </div>

    <!-- Main Report Body Container -->
    <div class="report-content">
        <h1>🌳 تقرير المعمار الشجري للمستندات والملفات</h1>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 24px;">نظام أرشفة وهندسة هيكل المجلدات الرقمي الأكاديمي.</p>

        <div class="search-container">
            <input type="text" id="directory-search" placeholder="🔍 ابدأ بكتابة اسم الملف أو المجلد للتصفية الفورية..." onkeyup="filterTree()">
        </div>

        <div class="controls-toolbar">
            <div class="btn-group">
                <button class="btn" onclick="toggleAll(true)">📂 توسيع الكل</button>
                <button class="btn" onclick="toggleAll(false)">📁 طي الكل</button>
            </div>
            <div class="btn-group">
                <button class="btn btn-accent" id="copy-text-btn" onclick="copyRawAsciiTree()">📋 نسخ الهيكل النصي</button>
            </div>
        </div>

        <!-- Tabs System to Toggle View Modes -->
        <div class="tabs-header">
            <button class="tab-btn active" id="btn-tree" onclick="switchTab('tree')">🌳 المعمار الشجري للمجلدات (Folder Tree)</button>
            <button class="tab-btn" id="btn-smart" onclick="switchTab('smart')">🔍 الفهرس والأرشفة المصنفة تلقائياً (Smart Categorization)</button>
        </div>

        <!-- Tab 1: Tree View -->
        <div class="tab-content active" id="tab-tree">
            <!-- Scrollable Tree Structure -->
            <div class="tree-container-root" id="tree-contents-panel">
""")
        
        // Write the HTML representation for child tree nodes
        writeHtmlNode(node, outWriter, showSize, showMtime, showCount)

        outWriter.write("""
            </div>
        </div>

        <!-- Tab 2: Smart Indexing & Categorized View -->
        <div class="tab-content" id="tab-smart">
""")
        
        generateSmartArchiveHtml(node, outWriter)

        outWriter.write("""
        </div>
        
        <!-- Raw TXT container for clipboard copying -->
        <textarea id="raw-ascii-data" class="raw-tree-container">""")
        
        // Write ASCII view right to hidden textbox for easy browser clipboard access
        val lines = generateTextLines(node.children, "", showSize, showMtime, showCount)
        outWriter.write("${node.name}\n")
        for (l in lines) {
            outWriter.write(l + "\n")
        }

        outWriter.write("""</textarea>

        <footer>
            المعمار الشجري - د. محمد المحطوري | تم التوليد بنجاح عبر تطبيق الأرشفة الرقمي للأندرويد.
        </footer>
    </div>
</div>

<script>
    // Tab switching engine
    function switchTab(tabId) {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
        
        document.getElementById('btn-' + tabId).classList.add('active');
        document.getElementById('tab-' + tabId).classList.add('active');
    }

    // Dynamically calculate and fill statistic counters inside browser page
    document.addEventListener("DOMContentLoaded", function() {
        const folders = document.querySelectorAll('details').length;
        const totalItems = document.querySelectorAll('.tree-node-item').length;
        const filesOnly = totalItems - folders;
        
        document.getElementById('folders-count').innerText = folders;
        document.getElementById('files-count').innerText = filesOnly >= 0 ? filesOnly : 0;

        // Enhance details elements with smooth height collapse/expand transitions
        setupSmoothCollapse();
    });

    function setupSmoothCollapse() {
        const detailsList = document.querySelectorAll('details');
        
        detailsList.forEach(details => {
            const summary = details.querySelector('summary');
            if (!summary) return;
            
            // Double click on summary to expand/collapse all children recursively
            summary.addEventListener('dblclick', function(e) {
                e.preventDefault();
                e.stopPropagation();
                const isOpen = details.hasAttribute('open');
                const childDetails = details.querySelectorAll('details');
                childDetails.forEach(cd => {
                    if (isOpen) {
                        cd.removeAttribute('open');
                    } else {
                        cd.setAttribute('open', 'true');
                    }
                });
                
                if (isOpen) {
                    slideUp(details, summary);
                } else {
                    slideDown(details, summary);
                }
            });
            
            summary.addEventListener('click', function(e) {
                e.preventDefault(); // Stop standard immediate toggle
                e.stopPropagation();
                
                if (details.hasAttribute('open')) {
                    slideUp(details, summary);
                } else {
                    slideDown(details, summary);
                }
            });
        });
    }

    function slideUp(details, summary) {
        details.style.overflow = 'hidden';
        const startHeight = details.offsetHeight;
        
        // Lock to current open height
        details.style.height = startHeight + 'px';
        
        // Force reflow
        details.offsetHeight;
        
        // Transition down to standard summary height
        const endHeight = summary.offsetHeight;
        details.style.transition = 'height 0.22s cubic-bezier(0.4, 0, 0.2, 1)';
        details.style.height = endHeight + 'px';
        
        const onFinish = function(e) {
            if (e.target !== details || e.propertyName !== 'height') return;
            details.removeAttribute('open');
            resetStyles(details);
            details.removeEventListener('transitionend', onFinish);
        };
        details.addEventListener('transitionend', onFinish);
    }

    function slideDown(details, summary) {
        // Set open to capture actual full height
        details.setAttribute('open', 'true');
        details.style.overflow = 'hidden';
        
        const endHeight = details.offsetHeight;
        const startHeight = summary.offsetHeight;
        
        details.style.height = startHeight + 'px';
        
        // Force reflow
        details.offsetHeight;
        
        details.style.transition = 'height 0.22s cubic-bezier(0.4, 0, 0.2, 1)';
        details.style.height = endHeight + 'px';
        
        const onFinish = function(e) {
            if (e.target !== details || e.propertyName !== 'height') return;
            resetStyles(details);
            details.removeEventListener('transitionend', onFinish);
        };
        details.addEventListener('transitionend', onFinish);
    }

    function resetStyles(details) {
        details.style.height = '';
        details.style.overflow = '';
        details.style.transition = '';
    }

    function toggleAll(open) {
        const details = document.querySelectorAll('details');
        details.forEach(d => {
            if (open) {
                d.setAttribute('open', 'true');
            } else {
                d.removeAttribute('open');
            }
            resetStyles(d);
        });
    }

    // Copy Ascii Tree to Clipboard
    function copyRawAsciiTree() {
        const textBox = document.getElementById('raw-ascii-data');
        textBox.style.display = 'block';
        textBox.select();
        document.execCommand('copy');
        textBox.style.display = 'none';
        
        const btn = document.getElementById('copy-text-btn');
        const origText = btn.innerText;
        btn.innerText = '✔️ تم النسخ بنجاح!';
        setTimeout(() => { btn.innerText = origText; }, 2000);
    }

    // Live search filter inside tree report
    function filterTree() {
        const query = document.getElementById('directory-search').value.toLowerCase().trim();
        const items = document.querySelectorAll('.tree-node-item');
        
        if (query === "") {
            items.forEach(it => {
                it.classList.remove('hidden');
                const nameNode = it.querySelector('.node-title');
                if (nameNode) {
                    nameNode.innerHTML = nameNode.getAttribute('data-original');
                }
            });
            return;
        }

        items.forEach(it => {
            const nameNode = it.querySelector('.node-title');
            if (nameNode) {
                const originalName = nameNode.getAttribute('data-original') || nameNode.textContent;
                if (!nameNode.hasAttribute('data-original')) {
                    nameNode.setAttribute('data-original', originalName);
                }

                const lowercaseName = originalName.toLowerCase();
                if (lowercaseName.includes(query)) {
                    it.classList.remove('hidden');
                    
                    // Highlighting matching substring
                    const regex = new RegExp('(' + query.replace(/[-\/\\^${'$'}*+?.()|[\]{}]/g, '\\$&') + ')', 'gi');
                    nameNode.innerHTML = originalName.replace(regex, '<span class="highlight">$1</span>');

                    // Recursive expand parent nodes
                    let parent = it.parentElement;
                    while (parent) {
                        if (parent.tagName === 'DETAILS') {
                            parent.setAttribute('open', 'true');
                            resetStyles(parent);
                        }
                        if (parent.tagName === 'LI') {
                            parent.classList.remove('hidden');
                        }
                        parent = parent.parentElement;
                    }
                } else {
                    it.classList.add('hidden');
                }
            }
        });
    }
</script>

</body>
</html>""")
    }

    fun writePdf(
        context: Context,
        node: TreeDocNode,
        outStream: java.io.OutputStream,
        rootName: String,
        showSize: Boolean,
        showMtime: Boolean,
        showCount: Boolean
    ) {
        val document = android.graphics.pdf.PdfDocument()
        
        // Generate total ASCII structure to draw line by line
        val lines = mutableListOf<String>()
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val rootExtras = mutableListOf<String>()
        if (showMtime && !node.mtime.isNullOrEmpty()) rootExtras.add("(${node.mtime})")
        if (showCount && node.count != null) rootExtras.add("📁 ${node.count} عناصر")
        val rootSuffix = if (rootExtras.isNotEmpty()) " " + rootExtras.joinToString(" ") else ""
        lines.add("${node.name}$rootSuffix")
        
        lines.addAll(generateTextLines(node.children, "", showSize, showMtime, showCount))
        
        val pageWidth = 595 // A4 standard width (points)
        val pageHeight = 842 // A4 standard height (points)
        val marginLeft = 40f
        val marginRight = 40f
        val marginTop = 50f
        val marginBottom = 50f
        
        // Drawing text pain setup
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1e293b") // Slate charcoal list item
            textSize = 9.5f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
            isAntiAlias = true
        }
        
        val bannerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0f172a") // Deep Granite title slate background
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }

        val headerTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 14f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }

        val subtextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#10b981") // Emerald accent
            textSize = 10f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 8.5f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#cbd5e1")
            strokeWidth = 1f
            style = android.graphics.Paint.Style.STROKE
        }

        var lineIndex = 0
        var pageNum = 1
        val maxLinesFirstPage = 28
        val maxLinesOtherPages = 38
        
        while (lineIndex < lines.size) {
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            var currentY = marginTop
            
            if (pageNum == 1) {
                // Header block
                canvas.drawRoundRect(30f, 30f, pageWidth - 30f, 135f, 12f, 12f, bannerPaint)
                canvas.drawText("المعمار الشجري - د. محمد المحطوري", 45f, 65f, headerTextPaint)
                canvas.drawText("تقرير هيكلية وترتيب ملفات النظام الرقمي", 45f, 90f, subtextPaint)
                canvas.drawText("المسار المستهدف: $rootName  |  التاريخ: $dateStr", 45f, 115f, metaPaint.apply { color = android.graphics.Color.parseColor("#94a3b8") })
                
                currentY = 165f
            } else {
                // Subheader standard tiny line
                canvas.drawLine(30f, 35f, pageWidth - 30f, 35f, linePaint)
                canvas.drawText("المعمار الشجري - د. محمد المحطوري | تقرير مستودع الأرشفة المنسق", 35f, 27f, metaPaint.apply { color = android.graphics.Color.GRAY })
                currentY = 55f
            }
            
            val maxLines = if (pageNum == 1) maxLinesFirstPage else maxLinesOtherPages
            var printedCount = 0
            
            while (lineIndex < lines.size && printedCount < maxLines) {
                val currentLineText = lines[lineIndex]
                
                // If it contains folders or categories, make it a bit colorful
                if (currentLineText.contains("📁") || currentLineText.contains("📂")) {
                    textPaint.color = android.graphics.Color.parseColor("#2563eb") // Soft blue directories
                } else {
                    textPaint.color = android.graphics.Color.parseColor("#334155") // Charcoal slate
                }
                
                canvas.drawText(currentLineText, marginLeft, currentY, textPaint)
                currentY += 16f
                lineIndex++
                printedCount++
            }
            
            // Render nice footer
            canvas.drawText("صفحة $pageNum", pageWidth / 2f - 15f, pageHeight - 30f, metaPaint.apply { color = android.graphics.Color.GRAY })
            
            document.finishPage(page)
            pageNum++
        }
        
        document.writeTo(outStream)
        document.close()
    }

    private fun writeHtmlNode(node: TreeDocNode, outWriter: BufferedWriter, showSize: Boolean, showMtime: Boolean, showCount: Boolean) {
        val isDir = node.type == "directory"
        val escName = escapeHtml(node.name)

        if (isDir) {
            outWriter.write("<details class=\"tree-node-item\" open>\n")
            outWriter.write("<summary class=\"folder-summary\">\n")
            outWriter.write("<span class=\"icon\">📁</span>")
            outWriter.write("<span class=\"node-title\" data-original=\"$escName\">$escName</span>\n")
            
            // Build badges metadata
            val badges = mutableListOf<String>()
            if (showMtime && !node.mtime.isNullOrEmpty()) {
                badges.add("<span class=\"badge mtime-badge\">🕒 ${escapeHtml(node.mtime!!)}</span>")
            }
            if (showCount && node.count != null) {
                badges.add("<span class=\"badge size-badge\">📂 ${node.count} عنصر</span>")
            }
            
            if (badges.isNotEmpty()) {
                outWriter.write("<div class=\"meta\">${badges.joinToString(" ")}</div>\n")
            }

            outWriter.write("</summary>\n")
            
            if (node.children.isNotEmpty()) {
                outWriter.write("<ul>\n")
                node.children.forEach { child ->
                    outWriter.write("<li>\n")
                    writeHtmlNode(child, outWriter, showSize, showMtime, showCount)
                    outWriter.write("</li>\n")
                }
                outWriter.write("</ul>\n")
            }
            outWriter.write("</details>\n")
        } else {
            // It's a file or error node
            val icon = if (node.type == "error") "⚠️" else getFileIcon(node.name)
            outWriter.write("<div class=\"file-item tree-node-item\">\n")
            outWriter.write("<span class=\"icon\">$icon</span>")
            outWriter.write("<span class=\"node-title\" data-original=\"$escName\">$escName</span>\n")

            // Meta specs
            val fileBadges = mutableListOf<String>()
            if (showSize && !node.size.isNullOrEmpty()) {
                fileBadges.add("<span class=\"badge size-badge\">💾 ${escapeHtml(node.size!!)}</span>")
            }
            if (showMtime && !node.mtime.isNullOrEmpty()) {
                fileBadges.add("<span class=\"badge mtime-badge\">🕒 ${escapeHtml(node.mtime!!)}</span>")
            }

            if (fileBadges.isNotEmpty()) {
                outWriter.write("<div class=\"meta\">${fileBadges.joinToString(" ")}</div>\n")
            }

            outWriter.write("</div>\n")
        }
    }

    private fun getFileIcon(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "html", "htm", "xhtml" -> "🌐"
            "css" -> "🎨"
            "js", "ts", "json" -> "💻"
            "pdf" -> "📕"
            "txt", "md", "csv", "log" -> "📄"
            "zip", "rar", "tar", "gz", "7z" -> "📦"
            "jpg", "jpeg", "png", "gif", "svg", "webp" -> "🖼️"
            "mp4", "mkv", "avi", "mov" -> "🎬"
            "mp3", "wav", "ogg", "flac" -> "🎵"
            "exe", "msi", "apk" -> "⚙️"
            else -> "📄"
        }
    }

    private fun collectAllFiles(node: TreeDocNode, result: MutableList<TreeDocNode>) {
        if (node.type == "file") {
            result.add(node)
        }
        node.children.forEach { collectAllFiles(it, result) }
    }

    private fun getSmartKeywordGroup(name: String): String? {
        val lower = name.lowercase()
        return when {
            lower.contains("طبي") || lower.contains("مرض") || lower.contains("علاج") || lower.contains("مستشفى") || lower.contains("روشت") -> "🩺 مذكرات ومستندات طبية (Medical Documents)"
            lower.contains("حق") || lower.contains("قانون") || lower.contains("حكم") || lower.contains("قضية") || lower.contains("دستور") || lower.contains("محام") -> "⚖️ أوراق ومستندات حقوقية وقانونية (Legal & Rights Document)"
            lower.contains("كتاب") || lower.contains("كتب") || lower.contains("رواية") || lower.contains("مكتبة") || lower.contains("مؤلف") -> "📚 مكتبة وكتب رقمية (Library & Digital Books)"
            lower.contains("تجميع") || lower.contains("ملخص") || lower.contains("مذكرة") || lower.contains("محاضرة") || lower.contains("مذكرات") -> "📝 تجميعات ومذكرات دراسية (Memos & Compilations)"
            lower.contains("بحث") || lower.contains("دراسة") || lower.contains("أطروحة") || lower.contains("علمي") || lower.contains("شرح") -> "🔬 بحوث ودراسات علمية (Scientific Research & Papers)"
            lower.contains("جديد") || lower.contains("محدث") || lower.contains("مسودة") || lower.contains("أخير") -> "✨ عناصر جديدة ومحدثة (New / Updated Elements)"
            else -> null
        }
    }

    private fun generateSmartArchiveHtml(node: TreeDocNode, outWriter: BufferedWriter) {
        val allFilesList = mutableListOf<TreeDocNode>()
        collectAllFiles(node, allFilesList)

        // 1. Keyword-based grouping
        val keywordGroups = mutableMapOf<String, MutableList<TreeDocNode>>()
        allFilesList.forEach { file ->
            val group = getSmartKeywordGroup(file.name)
            if (group != null) {
                keywordGroups.getOrPut(group) { mutableListOf() }.add(file)
            }
        }

        // 2. Class/Category-based grouping
        val categoryGroups = mutableMapOf<String, MutableList<TreeDocNode>>()
        allFilesList.forEach { file ->
            val niceCategory = when (file.category) {
                "صورة" -> "🖼️ صور رقمية وبطاقات مصورة (Images)"
                "مستند" -> "📄 مستندات نصية وقراءات أكاديمية (Documents & Books)"
                "فيديو" -> "🎥 ملفات مرئية ومقاطع مسجلة (Videos)"
                "صوت" -> "🎵 تسجيلات وملفات صوتية (Audio)"
                "ملف مضغوط" -> "📦 حزم ملفات مضغوطة وأرشيف (Archives)"
                "برمجيات/أكواد" -> "💻 مشاريع برمجية وشفرات مصدرية (Code)"
                "تطبيق/نظام" -> "⚙️ تطبيقات برمجية وأنظمة (Apps)"
                else -> "📝 ملفات متنوعة وأخرى (Others)"
            }
            categoryGroups.getOrPut(niceCategory) { mutableListOf() }.add(file)
        }

        outWriter.write("<div class=\"smart-archive-section\">\n")
        outWriter.write("<h3 style=\"color: var(--primary-color); border-bottom: 1.5px dashed var(--border-color); padding-bottom: 8px; margin-top: 0; font-size: 1.2rem;\">🔍 الأرشفة التلقائية على أساس الكلمات الدلالية الذكية (Smart Keyword Groups):</h3>\n")
        
        if (keywordGroups.isEmpty()) {
            outWriter.write("<p style=\"color: var(--text-muted); font-size: 0.85rem; background: rgba(255,255,255,0.02); padding: 12px; border-radius: 8px;\">لم يتم كشف أي مذكرات طبية، أو وثائق حقوقية، أو كتب وروايات، أو تجميعات دراسية بالكلمات الدلالية المألوفة في أسماء الملفات.</p>\n")
        } else {
            keywordGroups.forEach { (groupName, files) ->
                val escName = escapeHtml(groupName)
                outWriter.write("<details class=\"category-section\">\n")
                outWriter.write("  <summary class=\"category-header\">\n")
                outWriter.write("    <span>$escName</span>\n")
                outWriter.write("    <span class=\"badge size-badge\" style=\"background-color: var(--primary-color); color: #0b0f19;\">${files.size} ملف</span>\n")
                outWriter.write("  </summary>\n")
                outWriter.write("  <div class=\"category-body\">\n")
                files.forEach { file ->
                    val escFileName = escapeHtml(file.name)
                    val escPath = escapeHtml(file.relativePath.ifEmpty { file.absolutePath })
                    val sizeDisplay = if (file.size != null) " <span class=\"badge mtime-badge\" style=\"background: var(--hover-bg); border-color: transparent;\">${escapeHtml(file.size!!)}</span>" else ""
                    outWriter.write("    <div class=\"category-file-card\">\n")
                    outWriter.write("      <div class=\"cat-file-name\"><span>${getFileIcon(file.name)}</span>$escFileName $sizeDisplay</div>\n")
                    outWriter.write("      <div class=\"cat-file-path\">$escPath</div>\n")
                    outWriter.write("    </div>\n")
                }
                outWriter.write("  </div>\n")
                outWriter.write("</details>\n")
            }
        }

        outWriter.write("<h3 style=\"color: var(--accent-color); border-bottom: 1.5px dashed var(--border-color); padding-bottom: 8px; margin-top: 32px; font-size: 1.2rem;\">🏷️ الفرز الشجري الذكي حسب تصنيف الملف وامتداده (File Type Categories):</h3>\n")
        if (categoryGroups.isEmpty()) {
            outWriter.write("<p style=\"color: var(--text-muted); font-size: 0.85rem;\">لا توجد ملفات مصنفة في هذا المجلد.</p>\n")
        } else {
            categoryGroups.forEach { (categoryName, files) ->
                val escCat = escapeHtml(categoryName)
                outWriter.write("<details class=\"category-section\">\n")
                outWriter.write("  <summary class=\"category-header\">\n")
                outWriter.write("    <span>$escCat</span>\n")
                outWriter.write("    <span class=\"badge size-badge\" style=\"background-color: var(--accent-color); color: #ffffff;\">${files.size} ملف</span>\n")
                outWriter.write("  </summary>\n")
                outWriter.write("  <div class=\"category-body\">\n")
                files.forEach { file ->
                    val escFileName = escapeHtml(file.name)
                    val escPath = escapeHtml(file.relativePath.ifEmpty { file.absolutePath })
                    val sizeDisplay = if (file.size != null) " <span class=\"badge mtime-badge\" style=\"background: var(--hover-bg); border-color: transparent;\">${escapeHtml(file.size!!)}</span>" else ""
                    outWriter.write("    <div class=\"category-file-card\">\n")
                    outWriter.write("      <div class=\"cat-file-name\"><span>${getFileIcon(file.name)}</span>$escFileName $sizeDisplay</div>\n")
                    outWriter.write("      <div class=\"cat-file-path\">$escPath</div>\n")
                    outWriter.write("    </div>\n")
                }
                outWriter.write("  </div>\n")
                outWriter.write("</details>\n")
            }
        }
        outWriter.write("</div>\n")
    }

    private fun escapeHtml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    private fun escapeJson(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
