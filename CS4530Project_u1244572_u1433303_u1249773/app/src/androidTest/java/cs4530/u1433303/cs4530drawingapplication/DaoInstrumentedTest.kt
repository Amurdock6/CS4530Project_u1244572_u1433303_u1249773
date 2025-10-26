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

    /**
     * Creates a lightweight db to instantiate a dao instance.
     * Each test will be given a fresh db to work with.
     */
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

    /**
     * Simple test to insert 1 Bitmap image to the dao db. Checks if the count all drawings in the
     * db is simply 1.
     */
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

    /**
     * Adds 2 mock Bitmaps to the dao db, checks the count, deletes 1, then checks if the remaining
     * drawing is the one NOT chosen for deletion
     */
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

    /**
     * Adds 3 Bitmap images to the dao db, then checks if they exist in the resulting
     * getAllDrawings() Flow List
     *
     * This test checks each List item and ensures they aren't duplicates
     */
    @Test
    fun getAllDrawingsTest() = runBlocking{
        val testMap1 = BitmapFactory.decodeResource(appContext.resources, R.drawable.splash_screen_image)
        val testDrawing1 = DrawingEntity(1, "test1", testMap1, System.currentTimeMillis())
        testDao.insertDrawing(testDrawing1)

        val testMap2 = BitmapFactory.decodeResource(appContext.resources, R.drawable.splash_screen_image)
        val testDrawing2 = DrawingEntity(2, "test2", testMap2, System.currentTimeMillis())
        testDao.insertDrawing(testDrawing2)

        val testMap3 = BitmapFactory.decodeResource(appContext.resources, R.drawable.splash_screen_image)
        val testDrawing3 = DrawingEntity(3, "test3", testMap3, System.currentTimeMillis())
        testDao.insertDrawing(testDrawing3)

        val drawings : Flow<List<DrawingEntity>> = testDao.getAllDrawings()
        val drawingsList = drawings.first()
        val drawingCount = drawingsList.size
        assertTrue(drawingCount == 3)

        // NOTE: List is the resulting query of all drawings, sorted by decending ID's.
        // So here, id=3 "test3" would be [0], id=2 "test2" would be [1], and so on
        assertTrue(drawingsList[0].name == "test3")
        assertTrue(drawingsList[1].name == "test2")
        assertTrue(drawingsList[2].name == "test1")
    }
}