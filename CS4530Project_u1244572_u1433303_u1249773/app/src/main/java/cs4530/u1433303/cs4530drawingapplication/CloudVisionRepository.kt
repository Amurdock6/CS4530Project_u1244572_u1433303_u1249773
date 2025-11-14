package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap

/**
 * Repository for accessing the Google Cloud Vision API.
 */
class CloudVisionRepository {
    private val visionService = CloudVisionService()

    /**
     * Analyzes an image using the Cloud Vision API.
     * This is a suspend function that will run on a background thread.
     *
     * @param bitmap The image to analyze.
     * @return An [AnnotateImageResponse] or null if an error occurred.
     */
    suspend fun analyzeImage(bitmap: Bitmap): AnnotateImageResponse? {
        return visionService.analyzeImage(bitmap)
    }
}
