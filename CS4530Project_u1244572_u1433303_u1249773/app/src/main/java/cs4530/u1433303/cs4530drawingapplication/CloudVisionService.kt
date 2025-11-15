package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
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
data class NormalizedVertex(val x: Float? = null, val y: Float? = null)

@Serializable
data class BoundingPoly(val normalizedVertices: List<NormalizedVertex> = emptyList())

@Serializable
data class LocalizedObjectAnnotation(
    val name: String,
    val score: Float,
    val boundingPoly: BoundingPoly
)

@Serializable
data class Response(
    val labelAnnotations: List<EntityAnnotation>? = null,
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation>? = null
)

@Serializable
data class EntityAnnotation(val description: String, val score: Float)

class CloudVisionService {
    private val client: HttpClient = KtorClient.httpClient

    suspend fun analyzeImage(bitmap: Bitmap): AnnotateImageResponse? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.CLOUD_VISION_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_API_KEY_HERE") {
            Log.e("CloudVisionService", "API key is not set.")
            return@withContext null
        }

        // Encode bitmap to Base64 off the main thread
        val base64Image = run {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.DEFAULT)
        }

        val request = AnnotateImageRequest(
            requests = listOf(
                Request(
                    image = Image(base64Image),
                    features = listOf(
                        Feature(type = "LABEL_DETECTION"),
                        Feature(type = "OBJECT_LOCALIZATION")
                    )
                )
            )
        )

        return@withContext try {
                        val httpResponse = client.post("https://vision.googleapis.com/v1/images:annotate?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val text = httpResponse.bodyAsText()
            if (!httpResponse.status.isSuccess()) {
                // Try to parse Google error payload for better logs
                runCatching {
                    val err = Json { ignoreUnknownKeys = true }.decodeFromString(ErrorPayload.serializer(), text)
                    Log.e("CloudVisionService", "Vision API error: code=${err.error?.code} status=${err.error?.status} msg=${err.error?.message}")
                }.onFailure {
                    Log.e("CloudVisionService", "Vision API HTTP ${httpResponse.status}: $text")
                }
                return@withContext null
            }

            // Parse successful response
            Json { ignoreUnknownKeys = true }.decodeFromString(AnnotateImageResponse.serializer(), text)
        } catch (e: Exception) {
            Log.e("CloudVisionService", "Error calling Vision API", e)
            null
        }
    }
}


@kotlinx.serialization.Serializable
data class ErrorPayload(val error: GoogleError? = null)

@kotlinx.serialization.Serializable
data class GoogleError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
