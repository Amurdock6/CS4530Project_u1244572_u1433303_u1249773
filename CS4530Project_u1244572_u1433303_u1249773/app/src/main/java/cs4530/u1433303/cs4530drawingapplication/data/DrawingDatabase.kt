package cs4530.u1433303.cs4530drawingapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DrawingEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
}
