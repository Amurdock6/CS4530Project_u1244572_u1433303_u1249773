package cs4530.u1433303.cs4530drawingapplication.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val content: Bitmap,
    val createdAt: Long = System.currentTimeMillis()
)
