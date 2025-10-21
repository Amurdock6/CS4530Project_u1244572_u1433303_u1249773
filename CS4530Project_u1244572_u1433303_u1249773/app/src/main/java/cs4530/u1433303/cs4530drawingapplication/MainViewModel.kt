package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs4530.u1433303.cs4530drawingapplication.data.DrawingEntity
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: DrawingRepository) : ViewModel() {

    val drawings = repository.getAllDrawings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDrawing(name: String, data: Bitmap) {
        viewModelScope.launch {
            repository.saveDrawing(name, data)
        }
    }

    fun deleteDrawing(drawing: DrawingEntity) {
        viewModelScope.launch {
            repository.deleteDrawing(drawing)
        }
    }
}
