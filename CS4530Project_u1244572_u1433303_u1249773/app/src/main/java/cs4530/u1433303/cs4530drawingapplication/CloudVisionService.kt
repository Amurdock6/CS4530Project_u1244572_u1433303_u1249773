package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream

// data classes for the Vision API request and response
@Serializable
data class AnnotateImageRequest(val requests: List<Request>)

@Serializable
data class Request(val image: Image, val features: List<Feature>)

@Serializable
data class Image(val content: String)

@Serializable
data class Feature(val type: String, val maxResults: Int = 10)

@Serializable
data class AnnotateImageResponse(val responses: List<Response>)

@Serializable
data class Response(
    val labelAnnotations: List<EntityAnnotation>? = null,
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation>? = null
)

@Serializable
data class EntityAnnotation(val description: String, val score: Float, val boundingPoly: BoundingPoly? = null)

@Serializable
data class LocalizedObjectAnnotation(
    val name: String,
    val score: Float,
    val boundingPoly: BoundingPoly? = null
)

@Serializable
data class BoundingPoly(val normalizedVertices: List<NormalizedVertex>)

@Serializable
data class NormalizedVertex(val x: Float? = 0f, val y: Float? = 0f)


class CloudVisionService {
    private val client: HttpClient = KtorClient.httpClient

    suspend fun analyzeImage(bitmap: Bitmap): AnnotateImageResponse? {
        val apiKey = BuildConfig.CLOUD_VISION_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
            Log.e("CloudVisionService", "API key is not set.")
            return null
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val base64Image = android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.DEFAULT)

        val request = AnnotateImageRequest(
            requests = listOf(
                Request(
                    image = Image(base64Image),
                    features = listOf(
                        Feature(type = "OBJECT_LOCALIZATION"),
                        Feature(type = "LABEL_DETECTION")
                    )
                )
            )
        )

        return try {
            client.post("https://vision.googleapis.com/v1/images:annotate?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AnnotateImageResponse>()
        } catch (e: Exception) {
            Log.e("CloudVisionService", "Error calling Vision API", e)
            null
        }
    }
}
