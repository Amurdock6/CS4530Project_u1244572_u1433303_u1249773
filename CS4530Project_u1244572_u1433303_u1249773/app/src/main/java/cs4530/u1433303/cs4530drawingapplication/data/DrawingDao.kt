package cs4530.u1433303.cs4530drawingapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY id DESC")
    fun getAllDrawings(): Flow<List<DrawingEntity>>

    @Query("SELECT COUNT(*) FROM drawings")
    fun getNumberOfDrawings(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity)

    @Delete
    suspend fun deleteDrawing(drawing: DrawingEntity)
}
