package com.pson.myalarm.core.worker

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AudioFileCopyWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        const val WORK_NAME = "audio_copy_work"
        const val KEY_INPUT_URI = "input_uri"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_RESULT_URI = "result_uri"
        private const val AUDIO_DIR = "alarm_sounds"
    }

    override suspend fun doWork(): Result {
        val inputUri = inputData.getString(KEY_INPUT_URI) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()

        return try {
            val destinationFile = createAudioFile(fileName)
            // If existed then there is nothing to do
            if (!destinationFile.exists()) {
                copyAudioFile(Uri.parse(inputUri), destinationFile)
            }
            val outputData = workDataOf(
                KEY_RESULT_URI to destinationFile.toUri().toString(),
            )
            Result.success(outputData)
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createAudioFile(fileName: String): File {
        val audioDir = File(context.filesDir, AUDIO_DIR).apply {
            if (!exists()) mkdirs()
        }
        return File(audioDir, fileName)
    }

    private suspend fun copyAudioFile(sourceUri: Uri, destFile: File) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}