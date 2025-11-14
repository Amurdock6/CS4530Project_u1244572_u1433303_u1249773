package cs4530.u1433303.cs4530drawingapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository

class ViewModelFactory(
    private val drawingRepository: DrawingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(drawingRepository) as T
        }
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawingViewModel(drawingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
