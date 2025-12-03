package cs4530.u1433303.cs4530drawingapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val user: FirebaseUser? = Firebase.auth.currentUser,
    val error: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // keep state in sync with Firebase
        auth.addAuthStateListener { firebaseAuth ->
            _uiState.update { it.copy(user = firebaseAuth.currentUser) }
        }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun signIn() {
        viewModelScope.launch {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Sign-in failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            val email = _uiState.value.email.trim()
            val password = _uiState.value.password
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Sign-up failed") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
