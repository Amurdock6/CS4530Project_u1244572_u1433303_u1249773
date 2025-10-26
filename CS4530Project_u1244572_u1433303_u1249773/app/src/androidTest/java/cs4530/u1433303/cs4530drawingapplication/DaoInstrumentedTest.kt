package cs4530.u1433303.cs4530drawingapplication

import android.content.Context
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDao
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDatabase
import cs4530.u1433303.cs4530drawingapplication.data.DrawingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoInstrumentedTest {
    lateinit var testDao : DrawingDao
    val appContext : Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setupDao(){
        // Creates a lightweight db that requires only a instrumented test Context
        val db = Room.inMemoryDatabaseBuilder(
            appContext,
            DrawingDatabase::class.java
        ).allowMainThreadQueries().build()
        // Instantiate the test dao
        testDao = db.drawingDao()
    }

    @Test
    fun insertTest() = runBlocking { // lets dao 'suspend' functions run here
        // Create a mock Bitmap, store it in a DrawingEntity, then stores the Entity into the dao db
        val testMap = BitmapFactory.decodeResource(appContext.resources, R.drawable.splash_screen_image)
        val testDrawing = DrawingEntity(0, "test1", testMap, System.currentTimeMillis())
        testDao.insertDrawing(testDrawing)

        // Test if the insertion was a success
        val drawingCount : Int = testDao.getNumberOfDrawings()
        assertTrue(drawingCount == 1)
    }

    @Test
    fun deleteTest() = runBlocking {
        // add in 2 images to the db
        val testMap1 = BitmapFactory.decodeResource(appContext.resources, R.drawable.splash_screen_image)
        val testDrawing1 = DrawingEntity(1, "test1", testMap1, System.currentTimeMillis())
        testDao.insertDrawing(testDrawing1)

        val testMap2 = BitmapFactory.decodeResource(appContext.resources, R.drawable.splash_screen_image)
        val testDrawing2 = DrawingEntity(2, "test2", testMap2, System.currentTimeMillis())
        testDao.insertDrawing(testDrawing2)

        var drawingCount = testDao.getNumberOfDrawings()
        assertTrue(drawingCount == 2)

        // remove 1 image and check if only 1 remains
        testDao.deleteDrawing(testDrawing1)
        drawingCount = testDao.getNumberOfDrawings()
        assertTrue(drawingCount == 1)

        // Check if the DrawingEntity inside the db IS testDrawing2
        val drawings : Flow<List<DrawingEntity>> = testDao.getAllDrawings()
        val drawingsList = drawings.first()
        val remainingDrawing = drawingsList[0]
        assertTrue(testDrawing2.name == remainingDrawing.name)
    }
}