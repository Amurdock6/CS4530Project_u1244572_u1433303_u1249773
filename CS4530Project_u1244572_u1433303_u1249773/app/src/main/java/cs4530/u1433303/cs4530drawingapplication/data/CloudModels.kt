package cs4530.u1433303.cs4530drawingapplication.data

data class CloudDrawingMetadata(
    val id: String,
    val title: String,
    val imageUrl: String,
    val timestamp: Long,
    val ownerId: String
)

data class SharedDrawingMetadata(
    val id: String,
    val title: String,
    val imageUrl: String,
    val timestamp: Long,
    val senderId: String,
    val senderEmail: String,
    val receiverEmail: String
)
