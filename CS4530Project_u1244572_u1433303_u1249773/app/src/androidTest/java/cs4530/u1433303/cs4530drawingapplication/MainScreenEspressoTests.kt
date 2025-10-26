package cs4530.u1433303.cs4530drawingapplication

import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDao
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDatabase
import cs4530.u1433303.cs4530drawingapplication.data.DrawingEntity
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


// TODO: Create tests for the main Screen

@RunWith(AndroidJUnit4::class)
class MainScreenEspressoTests {
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    lateinit var dvm : DrawingViewModel
    lateinit var mvm : MainViewModel
    lateinit var appContext : Context
    lateinit var dao : DrawingDao
    lateinit var repo : DrawingRepository

    /**
     * Initializes a new MainScreen using the Constant values
     * before each test.
     */
    @Before
    fun setUp(){
        appContext = ApplicationProvider.getApplicationContext()
        val db = Room.databaseBuilder(
            appContext,
            DrawingDatabase::class.java,
            "drawing_db"
        ).fallbackToDestructiveMigration().build()
        dao = db.drawingDao()

        repo = DrawingRepository.getInstance(appContext, dao)

        dvm = DrawingViewModel(repo)
        mvm = MainViewModel(repo)

        composeTestRule.setContent {
            // We are omitting the JP-Nav stuff here, we're just testing this screen alone
            MainScreen(mvm, onNewDrawing = dvm::clearCanvas, onOpenDrawing = dvm::loadDrawing)
        }


    }

    @After
    fun stall(){
        Thread.sleep(1000)
    }

    /**
     * Tests to see if adding 3 images to the db will show on the MainScreen.
     * Test will check against their generated testTags (which should be a drawings 'name' property)
     */
    @Test
    fun drawingListTest() { // allows for insertDrawing dao operation to finish before anything else
        runBlocking {
            dao.insertDrawing(
                DrawingEntity(
                    1,
                    "test1",
                    BitmapFactory.decodeResource(
                        appContext.resources,
                        R.drawable.splash_screen_image
                    ),
                    System.currentTimeMillis()
                )
            )
            dao.insertDrawing(
                DrawingEntity(
                    2,
                    "test2",
                    BitmapFactory.decodeResource(
                        appContext.resources,
                        R.drawable.splash_screen_image
                    ),
                    System.currentTimeMillis()
                )
            )
            dao.insertDrawing(
                DrawingEntity(
                    3,
                    "test3",
                    BitmapFactory.decodeResource(
                        appContext.resources,
                        R.drawable.splash_screen_image
                    ),
                    System.currentTimeMillis()
                )
            )
        }
        Thread.sleep(1000) // wait for UI to recognize new testTag addition
        composeTestRule.onNode(hasTestTag("test1"))
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("test2"))
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("test3"))
            .assertExists()
            .assertIsDisplayed()

    }
}