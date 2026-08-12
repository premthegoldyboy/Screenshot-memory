package com.example.screenshotmemory.data.ocr

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OcrManager(private val context: Context) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun extractTextFromUri(uriString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val inputImage = InputImage.fromFilePath(context, uri)
            val visionText = Tasks.await(recognizer.process(inputImage))
            val extractedText = visionText.text.trim()
            Result.success(extractedText)
        } catch (e: Exception) {
            Log.e("OcrManager", "Failed to extract text from $uriString", e)
            Result.failure(e)
        }
    }
}
