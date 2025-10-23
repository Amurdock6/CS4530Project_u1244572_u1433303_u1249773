package cs4530.u1433303.cs4530drawingapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DrawingRepository private constructor(
    private val dao: DrawingDao,
    private val context: Context
) {
    fun getAllDrawings(): Flow<List<DrawingEntity>> = dao.getAllDrawings()

    suspend fun saveDrawing(
        name: String,
        bitmap: Bitmap
    ) {
        withContext(Dispatchers.IO) {
            val filename = "drawing_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // store record in DB
            val entity = DrawingEntity(
                name = filename,
                content = bitmap,
            )
            dao.insertDrawing(entity)
        }
    }

    suspend fun deleteDrawing(entity: DrawingEntity) {
        withContext(Dispatchers.IO) {
            // delete the image file first
            val file = File(context.filesDir, entity.name)
            if (file.exists()) file.delete()

            dao.deleteDrawing(entity)
        }
    }

    suspend fun saveDrawingFromView(canvasView: View) {
        withContext(Dispatchers.IO) {
            val bitmap: Bitmap = canvasView.drawToBitmap()

            // Save bitmap to file
            val filename = "drawing_${UUID.randomUUID()}.png"
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Save metadata in Room
            val entity = DrawingEntity(
                name = filename,
                content = bitmap,
            )
            dao.insertDrawing(entity)
        }
    }

    // Overwrite an existing drawing (same DB row + same file name)
    suspend fun updateExistingFromView(canvasView: View, existing: DrawingEntity) {
        withContext(Dispatchers.IO) {
            val bitmap: Bitmap = canvasView.drawToBitmap()

            // overwrite the same file name
            val file = File(context.filesDir, existing.name)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // replace the same row by using the same primary key id
            val updated = existing.copy(content = bitmap)
            dao.insertDrawing(updated) // REPLACE strategy in DAO keeps same id
        }
    }


    companion object {
        @Volatile private var INSTANCE: DrawingRepository? = null
        fun getInstance(ctx: Context, dao: DrawingDao): DrawingRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DrawingRepository(dao, ctx.applicationContext).also { INSTANCE = it }
            }
    }
}
