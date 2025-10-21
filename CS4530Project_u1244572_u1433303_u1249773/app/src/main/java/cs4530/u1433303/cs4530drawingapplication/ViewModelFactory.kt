package cs4530.u1433303.cs4530drawingapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDao
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository

class ViewModelFactory(private val repository: DrawingRepository, private val dao: DrawingDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawingViewModel(repository, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
