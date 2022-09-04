package com.example.background.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI


private const val TAG = "BlurWorker"
class BlurWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        // Display a status notification
        makeStatusNotification("Blurring image", appContext)
        // slows down the work so
        // it's easier to see each WorkRequest start, even on emulated devices
        sleep()

        return try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            // Create a Bitmap from the cupcake image
            val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))
            val output: Bitmap = blurBitmap(picture, appContext)
            // Write bitmap to a temp file
            val outputUri: Uri = writeBitmapToFile(appContext, output)
            val outputData: Data = workDataOf(KEY_IMAGE_URI to outputUri.toString())

            Result.success(outputData)

        } catch(throwable: Throwable) {
            Log.e(TAG, "Error applying blur")
            throwable.printStackTrace()

            Result.failure()
        }
    }
}