package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs4530.u1433303.cs4530drawingapplication.data.CloudDrawingMetadata
import cs4530.u1433303.cs4530drawingapplication.data.CloudSyncRepository
import cs4530.u1433303.cs4530drawingapplication.data.SharedDrawingMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CloudUiState(
    val userDrawings: List<CloudDrawingMetadata> = emptyList(),
    val sharedWithMe: List<SharedDrawingMetadata> = emptyList(),
    val sharedByMe: List<SharedDrawingMetadata> = emptyList(),
    val isBusy: Boolean = false,
    val message: String? = null
)

class CloudSyncViewModel(
    private val cloudRepo: CloudSyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudUiState())
    val uiState = _uiState.asStateFlow()

    fun refreshUserData(userId: String?, email: String?) {
        if (userId.isNullOrBlank()) {
            _uiState.value = CloudUiState(message = "Sign in to sync")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, message = null)
            try {
                val drawings = cloudRepo.loadUserDrawings(userId)
                val shared = cloudRepo.loadSharedDrawingsForEmail(email.orEmpty())
                val sharedByMe = cloudRepo.loadSharedDrawingsBySender(userId)
                _uiState.value = _uiState.value.copy(
                    userDrawings = drawings,
                    sharedWithMe = shared,
                    sharedByMe = sharedByMe
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Cloud sync failed: ${e.message ?: "Unknown error"}. Ensure you are signed in and Firestore rules allow authenticated access."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
            }
        }
    }

    fun uploadDrawing(title: String, bitmap: Bitmap, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, message = null)
            try {
                val uploaded = cloudRepo.uploadDrawingToCloud(title, bitmap)
                uploaded?.let {
                    _uiState.value = _uiState.value.copy(
                        userDrawings = (listOf(it) + _uiState.value.userDrawings)
                            .distinctBy { item -> item.id }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Upload failed: ${e.message ?: "Unknown error"}. Check sign-in status and Firestore rules."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
                onComplete?.invoke()
            }
        }
    }

    fun shareDrawing(title: String, bitmap: Bitmap, receiverEmail: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, message = null)
            try {
                val shared = cloudRepo.shareDrawingWithEmail(receiverEmail, title, bitmap)
                shared?.let {
                    _uiState.value = _uiState.value.copy(
                        sharedByMe = (listOf(it) + _uiState.value.sharedByMe)
                            .distinctBy { item -> item.id }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Share failed: ${e.message ?: "Unknown error"}. Check sign-in status and Firestore rules."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
                onComplete?.invoke()
            }
        }
    }

    fun unshareDrawing(sharedId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, message = null)
            try {
                cloudRepo.unshareDrawing(sharedId)
                val updatedShared = _uiState.value.sharedWithMe.filterNot { it.id == sharedId }
                val updatedSharedByMe = _uiState.value.sharedByMe.filterNot { it.id == sharedId }
                _uiState.value = _uiState.value.copy(
                    sharedWithMe = updatedShared,
                    sharedByMe = updatedSharedByMe
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Unshare failed: ${e.message ?: "Unknown error"}. Check sign-in status and Firestore rules."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
            }
        }
    }

    fun importDrawing(imageUrl: String, onSuccess: (Bitmap) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, message = null)
            try {
                val bmp = cloudRepo.fetchBitmap(imageUrl)
                onSuccess(bmp)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Download failed: ${e.message ?: "Unknown error"}. Check network and permissions."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
            }
        }
    }

    fun updateSharedWithMe(items: List<SharedDrawingMetadata>) {
        _uiState.value = _uiState.value.copy(sharedWithMe = items)
    }

    fun updateUserDrawings(items: List<CloudDrawingMetadata>) {
        _uiState.value = _uiState.value.copy(userDrawings = items)
    }

    fun updateSharedByMe(items: List<SharedDrawingMetadata>) {
        _uiState.value = _uiState.value.copy(sharedByMe = items)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
