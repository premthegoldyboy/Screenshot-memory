package com.example.screenshotmemory.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.screenshotmemory.data.db.ScreenshotEntity

class MediaStoreScanner(private val context: Context) {

    fun scanScreenshots(): List<ScreenshotEntity> {
        val screenshots = mutableListOf<ScreenshotEntity>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projectionList = mutableListOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Images.Media.RELATIVE_PATH)
            } else {
                add(MediaStore.Images.Media.DATA)
            }
        }

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projectionList.toTypedArray(),
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                } else {
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                }

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val filename = cursor.getString(nameColumn) ?: "Screenshot_$id.png"
                    val dateAddedSec = cursor.getLong(dateAddedColumn)
                    val dateModifiedSec = cursor.getLong(dateModifiedColumn)
                    val size = cursor.getLong(sizeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)

                    val path = if (pathColumn != -1) cursor.getString(pathColumn) else ""
                    val pathLower = path?.lowercase() ?: ""
                    val filenameLower = filename.lowercase()

                    val isScreenshot = pathLower.contains("screenshot") ||
                            filenameLower.contains("screenshot") ||
                            filenameLower.startsWith("screen_") ||
                            filenameLower.startsWith("scr_") ||
                            pathLower.contains("screencap")

                    if (isScreenshot) {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()

                        val dateTakenMillis = if (dateAddedSec > 0) dateAddedSec * 1000L else System.currentTimeMillis()
                        val dateModifiedMillis = if (dateModifiedSec > 0) dateModifiedSec * 1000L else System.currentTimeMillis()

                        screenshots.add(
                            ScreenshotEntity(
                                uri = contentUri,
                                filename = filename,
                                relativePath = path,
                                dateTaken = dateTakenMillis,
                                dateModified = dateModifiedMillis,
                                size = size,
                                width = width,
                                height = height,
                                indexingStatus = ScreenshotEntity.INDEX_STATUS_PENDING,
                                isScreenshot = true
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MediaStoreScanner", "Error scanning MediaStore", e)
        }

        return screenshots
    }

    fun verifyUriExists(uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
