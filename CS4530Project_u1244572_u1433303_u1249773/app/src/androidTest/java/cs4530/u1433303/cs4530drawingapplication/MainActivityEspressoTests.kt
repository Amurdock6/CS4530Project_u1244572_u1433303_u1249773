package cs4530.u1433303.cs4530drawingapplication

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTests {
    // Setup an isolated lightweight activity that will hold ONLY our composable that needs testing
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // vm will be referenced in tests to check values of pen properties
    // vm.uiState.value holds pen properties
    lateinit var vm : DrawingViewModel

    // Constants for either initialization or testing against changes
    private val INITIAL_PEN_SIZE = 25.0f
    private val INITIAL_PEN_COLOR = Color.Red
    private val INITIAL_PEN_SHAPE = BrushShape.Round
    private val TEST_STALL_PERIOD : Long = 2000 // in milliseconds

    /**
     * Initializes a new DrawingViewModel using the Constant values
     * before each test.
     */
    @Before
    fun setUp(){

        vm = DrawingViewModel()
        vm.setBrushShape(INITIAL_PEN_SHAPE)
        vm.setBrushColor(INITIAL_PEN_COLOR)
        vm.setBrushSize(INITIAL_PEN_SIZE)

        composeTestRule.setContent {
            DrawingAppScreen(vm)
        }
    }

    /**
     * Stalls each test after they run so that we can observe test results visually.
     */
    @After
    fun stall(){
        Thread.sleep(TEST_STALL_PERIOD)
    }

    // TODO: Split tests into smaller unit tests
    //  (i.e: a test to check if a composable exists, then a test that just interfaces with
    //  with the composable whilst assuming it exists)

    /**
     * 3 part test that checks if we can draws on the DrawingAppScreen's
     * drawCanvas composable.
     *
     * Checks if the composable exists, is displayed, and then
     * draws on the screen (THIS IS A VISUAL TEST FOR NOW)
     */
    @Test
    fun simpleDrawTest() {
        composeTestRule.onNode(hasTestTag("drawCanvas"))
            .assertIsDisplayed()
            .assertExists()
            .performTouchInput { swipeLeft() }
            .performTouchInput { swipeUp() }
    }

    /**
     * Effectively checks to see if DrawingAppScreen's squareBrushButton
     * composable can be selected.
     *
     * First clicks the squareBrush. Then checks programmatically if the
     * composable isSelected.
     */
    @Test
    fun selectSquareBrushTest(){
        composeTestRule.onNode(hasTestTag("squareBrushButton"))
            .assertExists()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNode(hasTestTag("squareBrushButton"))
            .assertIsSelected()
    }

    /**
     * Checks if the slider can be adjusted to a larger value
     * than default (see INITIAL_PEN_SIZE constant)
     */
    @Test
    fun setBrushSizeLargeAndDrawTest(){
        composeTestRule.onNode(hasTestTag("brushSizeSlider"))
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("brushSizeSlider"))
            .performClick()
            .performTouchInput { swipeRight(300.0f, 600.0f) }
        assertTrue(vm.uiState.value.brushSize >= INITIAL_PEN_SIZE)
    }

    /**
     * Checks that the clearCanvasButton exists, is displayed,
     * and can actually clear the DrawingViewModel's list of Strokes
     * to that of an empty List.
     */
    @Test
    fun clearCanvasTest(){
        composeTestRule.onNode(hasTestTag("clearCanvasButton"))
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("drawCanvas"))
            .performTouchInput { swipeUp() }
            .performTouchInput { swipeRight() }
        Thread.sleep(1000)
        composeTestRule.onNode(hasTestTag("clearCanvasButton"))
            .performClick()
        assertTrue(vm.uiState.value.strokes.isEmpty())
    }

}