package com.example.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class CopiedLocalFile(
    val uriString: String,
    val fileName: String,
    val sizeBytes: Long,
    val detectedKind: MediaKind,
)

class LocalMediaStorageManager(private val context: Context) {

    private val mediaDir: File by lazy {
        File(context.filesDir, "edutech_media").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun copyUriToLocalStorage(sourceUri: Uri): CopiedLocalFile = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(sourceUri) ?: ""
        val displayName = resolveDisplayName(sourceUri) ?: "media_${System.currentTimeMillis()}"
        val extension = resolveExtension(displayName, mimeType)
        val detectedKind = detectMediaKind(mimeType, extension)

        val safeBase = displayName
            .substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_")
            .take(36)
            .ifBlank { "asset" }
        val uniqueName = "${safeBase}_${UUID.randomUUID().toString().take(8)}.$extension"
        val destFile = File(mediaDir, uniqueName)

        resolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output, bufferSize = 16 * 1024)
            }
        } ?: throw IllegalStateException("تعذّر قراءة الملف المختار من الجهاز")

        CopiedLocalFile(
            uriString = Uri.fromFile(destFile).toString(),
            fileName = displayName,
            sizeBytes = destFile.length(),
            detectedKind = detectedKind,
        )
    }

    suspend fun deleteLocalFileIfInternal(uriString: String) = withContext(Dispatchers.IO) {
        if (!uriString.startsWith("file://")) return@withContext
        val path = Uri.parse(uriString).path ?: return@withContext
        val file = File(path)
        if (file.exists() && file.parentFile?.absolutePath == mediaDir.absolutePath) {
            file.delete()
        }
    }

    suspend fun calculateTotalInternalStorageBytes(): Long = withContext(Dispatchers.IO) {
        mediaDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun resolveDisplayName(uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) return cursor.getString(idx)
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun resolveExtension(displayName: String, mimeType: String): String {
        val fromName = displayName.substringAfterLast('.', "").lowercase()
        if (fromName.isNotBlank() && fromName.length <= 5) return fromName
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.lowercase() ?: "bin"
    }

    private fun detectMediaKind(mimeType: String, ext: String): MediaKind {
        return when {
            mimeType.startsWith("video/") || ext in setOf("mp4", "mkv", "webm", "mov", "3gp") -> MediaKind.VIDEO
            mimeType.startsWith("audio/") || ext in setOf("mp3", "wav", "m4a", "aac", "ogg", "flac") -> MediaKind.AUDIO
            mimeType.startsWith("image/") || ext in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp") -> MediaKind.IMAGE
            else -> MediaKind.DOCUMENT
        }
    }
}
