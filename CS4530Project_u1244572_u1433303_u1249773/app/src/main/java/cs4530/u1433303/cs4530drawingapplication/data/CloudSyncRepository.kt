package cs4530.u1433303.cs4530drawingapplication.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class CloudSyncRepository private constructor() {

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val auth = Firebase.auth
    private val maxDownloadBytes = 5L * 1024 * 1024 // 5 MB guardrail

    private fun Bitmap.toPngBytes(): ByteArray {
        val baos = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    suspend fun uploadDrawingToCloud(title: String, bitmap: Bitmap): CloudDrawingMetadata? {
        val uid = auth.currentUser?.uid ?: return null
        val fileName = "${UUID.randomUUID()}.png"
        val ref = storage.reference.child("user_drawings/$uid/$fileName")
        ref.putBytes(bitmap.toPngBytes()).await()
        val url = ref.downloadUrl.await().toString()

        val doc = firestore.collection("user_drawings").document()
        val payload = mapOf(
            "userId" to uid,
            "imageUrl" to url,
            "timestamp" to Timestamp.now(),
            "title" to title
        )
        doc.set(payload).await()
        val ts = (payload["timestamp"] as Timestamp).seconds * 1000
        return CloudDrawingMetadata(doc.id, title, url, ts, uid)
    }

    suspend fun loadUserDrawings(uid: String): List<CloudDrawingMetadata> {
        val snap = firestore.collection("user_drawings")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp")
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val url = doc.getString("imageUrl") ?: return@mapNotNull null
            val title = doc.getString("title") ?: "Untitled"
            val ts = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
            val ownerId = doc.getString("userId") ?: uid
            CloudDrawingMetadata(
                id = doc.id,
                title = title,
                imageUrl = url,
                timestamp = ts,
                ownerId = ownerId
            )
        }.sortedByDescending { it.timestamp }
    }

    suspend fun shareDrawingWithEmail(
        receiverEmail: String,
        title: String,
        bitmap: Bitmap
    ): SharedDrawingMetadata? {
        val senderId = auth.currentUser?.uid ?: return null
        val fileName = "shared_${UUID.randomUUID()}.png"
        val ref = storage.reference.child("shared_drawings/$senderId/$fileName")
        ref.putBytes(bitmap.toPngBytes()).await()
        val url = ref.downloadUrl.await().toString()

        val doc = firestore.collection("shared_drawings").document()
        val payload = mapOf(
            "imageUrl" to url,
            "senderId" to senderId,
            "receiverEmail" to receiverEmail.lowercase(),
            "timestamp" to Timestamp.now(),
            "title" to title
        )
        doc.set(payload).await()
        val ts = (payload["timestamp"] as Timestamp).seconds * 1000
        return SharedDrawingMetadata(
            id = doc.id,
            title = title,
            imageUrl = url,
            timestamp = ts,
            senderId = senderId,
            receiverEmail = receiverEmail
        )
    }

    suspend fun loadSharedDrawingsForEmail(email: String): List<SharedDrawingMetadata> {
        if (email.isBlank()) return emptyList()
        val snap = firestore.collection("shared_drawings")
            .whereEqualTo("receiverEmail", email.lowercase())
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val url = doc.getString("imageUrl") ?: return@mapNotNull null
            val title = doc.getString("title") ?: "Shared drawing"
            val ts = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
            val senderId = doc.getString("senderId") ?: ""
            val receiver = doc.getString("receiverEmail") ?: email
            SharedDrawingMetadata(
                id = doc.id,
                title = title,
                imageUrl = url,
                timestamp = ts,
                senderId = senderId,
                receiverEmail = receiver
            )
        }.sortedByDescending { it.timestamp }
    }

    suspend fun loadSharedDrawingsBySender(uid: String): List<SharedDrawingMetadata> {
        if (uid.isBlank()) return emptyList()
        val snap = firestore.collection("shared_drawings")
            .whereEqualTo("senderId", uid)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val url = doc.getString("imageUrl") ?: return@mapNotNull null
            val title = doc.getString("title") ?: "Shared drawing"
            val ts = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
            val receiver = doc.getString("receiverEmail") ?: ""
            SharedDrawingMetadata(
                id = doc.id,
                title = title,
                imageUrl = url,
                timestamp = ts,
                senderId = uid,
                receiverEmail = receiver
            )
        }.sortedByDescending { it.timestamp }
    }

    suspend fun unshareDrawing(sharedDocId: String) {
        firestore.collection("shared_drawings").document(sharedDocId).delete().await()
    }

    suspend fun fetchBitmap(imageUrl: String): Bitmap {
        val ref = storage.getReferenceFromUrl(imageUrl)
        val bytes = ref.getBytes(maxDownloadBytes).await()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Unable to decode image")
    }

    companion object {
        @Volatile private var INSTANCE: CloudSyncRepository? = null
        fun getInstance(): CloudSyncRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudSyncRepository().also { INSTANCE = it }
            }
    }
}
