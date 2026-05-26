package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AuthResult
import com.example.domain.repository.WBankRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiEvent {
    data class ShowSnackbar(val message: String) : AuthUiEvent
    object NavigateToDashboard : AuthUiEvent
}

data class AuthUiState(
    val isLogin: Boolean = true,
    
    // Core Field Inputs
    val email: String = "",
    val emailError: String? = null,
    
    val password: String = "",
    val passwordError: String? = null,
    
    val firstName: String = "",
    val firstNameError: String? = null,
    
    val lastName: String = "",
    val lastNameError: String? = null,
    
    val nationalId: String = "",
    val nationalIdError: String? = null,
    
    val isLoading: Boolean = false,
    val isAuthed: Boolean = false
)

class AuthViewModel(private val repository: WBankRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    private val numericRegex = Regex("^\\d+$")

    init {
        // Perform an initial check to see if we already have an active session
        viewModelScope.launch {
            val sessionUser = repository.loadCurrentUser()
            if (sessionUser != null) {
                _uiState.value = _uiState.value.copy(isAuthed = true)
                _uiEvent.emit(AuthUiEvent.NavigateToDashboard)
            }
        }
    }

    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLogin = !_uiState.value.isLogin,
            emailError = null,
            passwordError = null,
            firstNameError = null,
            lastNameError = null,
            nationalIdError = null
        )
    }

    fun onEmailChanged(value: String) {
        val error = when {
            value.isBlank() -> "Email cannot be empty"
            !emailRegex.matches(value) -> "Invalid Email format"
            else -> null
        }
        _uiState.value = _uiState.value.copy(email = value, emailError = error)
    }

    fun onPasswordChanged(value: String) {
        val error = when {
            value.isBlank() -> "Password cannot be empty"
            value.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        _uiState.value = _uiState.value.copy(password = value, passwordError = error)
    }

    fun onFirstNameChanged(value: String) {
        val error = if (value.isBlank()) "First name is required" else null
        _uiState.value = _uiState.value.copy(firstName = value, firstNameError = error)
    }

    fun onLastNameChanged(value: String) {
        val error = if (value.isBlank()) "Last name is required" else null
        _uiState.value = _uiState.value.copy(lastName = value, lastNameError = error)
    }

    fun onNationalIdChanged(value: String) {
        val error = when {
            value.isBlank() -> "National ID is required"
            value.length != 11 -> "National ID must be exactly 11 digits"
            !numericRegex.matches(value) -> "National ID must contain digits only"
            else -> null
        }
        _uiState.value = _uiState.value.copy(nationalId = value, nationalIdError = error)
    }

    fun submit() {
        val currentState = _uiState.value
        
        // Final validations before call
        if (currentState.isLogin) {
            val isEmailValid = currentState.email.isNotBlank() && currentState.emailError == null
            val isPasswordValid = currentState.password.isNotBlank() && currentState.passwordError == null
            
            if (!isEmailValid || !isPasswordValid) {
                if (currentState.email.isBlank()) onEmailChanged("")
                if (currentState.password.isBlank()) onPasswordChanged("")
                viewModelScope.launch {
                    _uiEvent.emit(AuthUiEvent.ShowSnackbar("Please fix the validation errors."))
                }
                return
            }

            performLogin()
        } else {
            val isEmailValid = currentState.email.isNotBlank() && currentState.emailError == null
            val isPasswordValid = currentState.password.isNotBlank() && currentState.passwordError == null
            val isFirstValid = currentState.firstName.isNotBlank() && currentState.firstNameError == null
            val isLastValid = currentState.lastName.isNotBlank() && currentState.lastNameError == null
            val isNationalValid = currentState.nationalId.isNotBlank() && currentState.nationalIdError == null

            if (!isEmailValid || !isPasswordValid || !isFirstValid || !isLastValid || !isNationalValid) {
                if (currentState.email.isBlank()) onEmailChanged("")
                if (currentState.password.isBlank()) onPasswordChanged("")
                if (currentState.firstName.isBlank()) onFirstNameChanged("")
                if (currentState.lastName.isBlank()) onLastNameChanged("")
                if (currentState.nationalId.isBlank()) onNationalIdChanged("")
                
                viewModelScope.launch {
                    _uiEvent.emit(AuthUiEvent.ShowSnackbar("Please verify your fields and try again."))
                }
                return
            }

            performSignup()
        }
    }

    private fun performLogin() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.login(
                email = _uiState.value.email,
                passwordHash = _uiState.value.password // Simulator checks exact matching password string
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isAuthed = true)
                    _uiEvent.emit(AuthUiEvent.NavigateToDashboard)
                }
                is AuthResult.Error -> {
                    _uiEvent.emit(AuthUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    private fun performSignup() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.signup(
                firstName = _uiState.value.firstName,
                lastName = _uiState.value.lastName,
                nationalId = _uiState.value.nationalId,
                email = _uiState.value.email,
                passwordHash = _uiState.value.password
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isAuthed = true)
                    _uiEvent.emit(AuthUiEvent.NavigateToDashboard)
                }
                is AuthResult.Error -> {
                    _uiEvent.emit(AuthUiEvent.ShowSnackbar(result.message))
                }
            }
        }
    }
}
