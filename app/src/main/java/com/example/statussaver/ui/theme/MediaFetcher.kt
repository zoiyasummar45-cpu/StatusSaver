package com.example.statussaver

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class MediaType { IMAGE, VIDEO, AUDIO }

data class StatusMedia(
    val uri: Uri,
    val mediaType: MediaType,
    val isVideo: Boolean = (mediaType == MediaType.VIDEO),
    val name: String = "",
    val isBusiness: Boolean = false
)

suspend fun fetchStatuses(
    context: Context,
    treeUriString: String,
    isBusiness: Boolean = false
): List<StatusMedia> = withContext(Dispatchers.IO) {
    val mediaList = mutableListOf<StatusMedia>()

    try {
        val treeUri = Uri.parse(treeUriString)
        val rootDocId = try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (_: Exception) {
            ""
        }

        fun queryDocId(docId: String): List<StatusMedia> {
            val list = mutableListOf<StatusMedia>()
            if (docId.isEmpty()) return list

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE
            )

            try {
                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                    while (cursor.moveToNext()) {
                        val docIdChild = if (idCol != -1) cursor.getString(idCol) else continue
                        val name = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
                        val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "" else ""
                        val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 1L

                        if (size > 0 && !name.startsWith(".")) {
                            val lower = name.lowercase()
                            val isImg = lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || mime.startsWith("image/")
                            val isVid = lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".3gp") || lower.endsWith(".webm") || mime.startsWith("video/")
                            val isAud = lower.endsWith(".opus") || lower.endsWith(".aac") || lower.endsWith(".m4a") || lower.endsWith(".mp3") || lower.endsWith(".ogg") || lower.endsWith(".wav") || lower.endsWith(".amr") || mime.startsWith("audio/")

                            val mType = when {
                                isImg -> MediaType.IMAGE
                                isVid -> MediaType.VIDEO
                                isAud -> MediaType.AUDIO
                                else -> null
                            }

                            if (mType != null) {
                                val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docIdChild)
                                val item = StatusMedia(fileUri, mediaType = mType, name = name, isBusiness = isBusiness)
                                if (!list.contains(item)) {
                                    list.add(item)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }

        // Candidate doc IDs for Simple WhatsApp vs WhatsApp Business
        val candidateDocIds = if (isBusiness) {
            listOf(
                "$rootDocId/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
                "$rootDocId/com.whatsapp.w4b/WhatsApp Business/Media/Status",
                "$rootDocId/WhatsApp Business/Media/.Statuses",
                "$rootDocId/WhatsApp Business/Media/Status",
                "primary:Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
                "primary:Android/media/com.whatsapp.w4b/WhatsApp Business/Media/Status",
                "primary:WhatsApp Business/Media/.Statuses"
            )
        } else {
            listOf(
                "$rootDocId/com.whatsapp/WhatsApp/Media/.Statuses",
                "$rootDocId/com.whatsapp/WhatsApp/Media/Status",
                "$rootDocId/WhatsApp/Media/.Statuses",
                "$rootDocId/WhatsApp/Media/Status",
                "$rootDocId/Media/.Statuses",
                "$rootDocId/.Statuses",
                "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
                "primary:Android/media/com.whatsapp/WhatsApp/Media/Status",
                "primary:WhatsApp/Media/.Statuses"
            )
        }

        for (docId in candidateDocIds) {
            val found = queryDocId(docId)
            if (found.isNotEmpty()) {
                mediaList.addAll(found)
                break
            }
        }

        // Fallback DocumentFile scan if ContentResolver returned nothing
        if (mediaList.isEmpty()) {
            val rootDir = DocumentFile.fromTreeUri(context, treeUri)
            if (rootDir != null && rootDir.exists()) {
                val targetFolderName = if (isBusiness) "com.whatsapp.w4b" else "com.whatsapp"
                val targetDir = if (rootDir.name == targetFolderName) rootDir else rootDir.findFile(targetFolderName)

                val statusesDir = targetDir?.findFile(if (isBusiness) "WhatsApp Business" else "WhatsApp")
                    ?.findFile("Media")
                    ?.findFile(".Statuses")
                    ?: rootDir.findFile(".Statuses")
                    ?: rootDir.findFile("Status")

                if (statusesDir != null && statusesDir.isDirectory) {
                    for (file in statusesDir.listFiles()) {
                        if (file.isFile && file.length() > 0) {
                            val name = file.name?.lowercase() ?: ""
                            if (name.startsWith(".")) continue
                            val isImg = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")
                            val isVid = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp") || name.endsWith(".webm")
                            val isAud = name.endsWith(".opus") || name.endsWith(".aac") || name.endsWith(".m4a") || name.endsWith(".mp3") || name.endsWith(".ogg") || name.endsWith(".wav")

                            val mType = when {
                                isImg -> MediaType.IMAGE
                                isVid -> MediaType.VIDEO
                                isAud -> MediaType.AUDIO
                                else -> null
                            }

                            if (mType != null) {
                                val item = StatusMedia(file.uri, mediaType = mType, name = file.name ?: "", isBusiness = isBusiness)
                                if (!mediaList.contains(item)) {
                                    mediaList.add(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext mediaList.reversed()
}

fun saveMediaToGallery(context: Context, media: StatusMedia): Boolean {
    return try {
        val inputStream = context.contentResolver.openInputStream(media.uri) ?: return false

        val subDir = when (media.mediaType) {
            MediaType.IMAGE -> Environment.DIRECTORY_PICTURES
            MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
            MediaType.AUDIO -> Environment.DIRECTORY_MUSIC
        }

        val targetDir = File(Environment.getExternalStoragePublicDirectory(subDir), "StatusSaver")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val fileExtension = when (media.mediaType) {
            MediaType.IMAGE -> if (media.name.endsWith(".png")) ".png" else ".jpg"
            MediaType.VIDEO -> ".mp4"
            MediaType.AUDIO -> if (media.name.endsWith(".opus")) ".opus" else ".mp3"
        }

        val timeStamp = System.currentTimeMillis()
        val fileName = "status_${timeStamp}${fileExtension}"
        val targetFile = File(targetDir, fileName)

        val outputStream = FileOutputStream(targetFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        android.media.MediaScannerConnection.scanFile(
            context,
            arrayOf(targetFile.absolutePath),
            null
        ) { _, _ -> }

        Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}

fun fetchSavedStatuses(context: Context): List<StatusMedia> {
    val list = mutableListOf<StatusMedia>()
    val folderName = "StatusSaver"

    val subDirs = listOf(
        Pair(Environment.DIRECTORY_PICTURES, MediaType.IMAGE),
        Pair(Environment.DIRECTORY_MOVIES, MediaType.VIDEO),
        Pair(Environment.DIRECTORY_MUSIC, MediaType.AUDIO)
    )

    for ((subDir, defaultType) in subDirs) {
        val targetDir = File(Environment.getExternalStoragePublicDirectory(subDir), folderName)
        if (targetDir.exists() && targetDir.isDirectory) {
            targetDir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() > 0) {
                    val name = file.name.lowercase()
                    val mType = when {
                        name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp") -> MediaType.IMAGE
                        name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp") || name.endsWith(".webm") -> MediaType.VIDEO
                        name.endsWith(".opus") || name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".wav") -> MediaType.AUDIO
                        else -> defaultType
                    }
                    list.add(StatusMedia(Uri.fromFile(file), mediaType = mType, name = file.name))
                }
            }
        }
    }
    return list.reversed()
}

fun deleteSavedMedia(context: Context, media: StatusMedia): Boolean {
    return try {
        val path = media.uri.path
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                }
                return deleted
            }
        }
        val docFile = DocumentFile.fromSingleUri(context, media.uri)
        val docDeleted = docFile?.delete() ?: false
        if (!docDeleted) {
            context.contentResolver.delete(media.uri, null, null) > 0
        } else {
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}