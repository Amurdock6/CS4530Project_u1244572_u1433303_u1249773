package cs4530.u1433303.cs4530drawingapplication

import android.view.FrameMetrics.ANIMATION_DURATION
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.slowSwipeLeft
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun drawPenTest(){
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            val mockVM = DrawingViewModel()
            mockVM.setBrushColor(Color.Red)
            mockVM.setBrushSize(50.0f)
            mockVM.setBrushShape(BrushShape.Square)

            DrawingScreen(mockVM)
        }

        composeTestRule.mainClock.advanceTimeBy(ANIMATION_DURATION.toLong() + 5L) /*add 5s buffer*/

        composeTestRule.onNodeWithTag("drawingScreen")
            .performTouchInput { slowSwipeLeft() }
        //Thread.sleep(5000)
        composeTestRule.mainClock.advanceTimeBy(ANIMATION_DURATION.toLong() + 5L) /*add 5s buffer*/
        composeTestRule.mainClock.autoAdvance = true
        Thread.sleep(5000)
    }

}