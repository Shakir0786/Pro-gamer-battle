package progamerbattle.com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import progamerbattle.com.data.AuthRepository
import progamerbattle.com.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val isLoginMode: Boolean = true,
    // Login form fields & errors
    val loginMobile: String = "",
    val loginPin: String = "",
    val loginMobileError: String? = null,
    val loginPinError: String? = null,
    val loginGeneralError: String? = null,
    // Registration form fields & errors
    val regFullName: String = "",
    val regMobile: String = "",
    val regIgn: String = "",
    val regInGameUid: String = "",
    val regPin: String = "",
    val regReferralCode: String = "",
    val regFullNameError: String? = null,
    val regMobileError: String? = null,
    val regIgnError: String? = null,
    val regInGameUidError: String? = null,
    val regPinError: String? = null,
    val regReferralCodeError: String? = null,
    val regGeneralError: String? = null,
    // Success feedback
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun setAuthMode(isLogin: Boolean) {
        _uiState.update {
            it.copy(
                isLoginMode = isLogin,
                loginMobileError = null,
                loginPinError = null,
                loginGeneralError = null,
                regFullNameError = null,
                regMobileError = null,
                regIgnError = null,
                regInGameUidError = null,
                regPinError = null,
                regReferralCodeError = null,
                regGeneralError = null,
                successMessage = null
            )
        }
    }

    fun onLoginMobileChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(loginMobile = filtered, loginMobileError = null, loginGeneralError = null) }
    }

    fun onLoginPinChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(loginPin = filtered, loginPinError = null, loginGeneralError = null) }
    }

    fun onRegFullNameChanged(value: String) {
        _uiState.update { it.copy(regFullName = value, regFullNameError = null, regGeneralError = null) }
    }

    fun onRegMobileChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(regMobile = filtered, regMobileError = null, regGeneralError = null) }
    }

    fun onRegIgnChanged(value: String) {
        _uiState.update { it.copy(regIgn = value, regIgnError = null, regGeneralError = null) }
    }

    fun onRegInGameUidChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(regInGameUid = filtered, regInGameUidError = null, regGeneralError = null) }
    }

    fun onRegPinChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(regPin = filtered, regPinError = null, regGeneralError = null) }
    }

    fun onRegReferralCodeChanged(value: String) {
        val uppercase = value.uppercase().filter { it.isLetterOrDigit() }.take(16)
        _uiState.update { it.copy(regReferralCode = uppercase, regReferralCodeError = null, regGeneralError = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, loginGeneralError = null, regGeneralError = null) }
    }

    fun login() {
        val state = _uiState.value
        var hasError = false
        var mobileError: String? = null
        var pinError: String? = null

        if (state.loginMobile.length != 10) {
            mobileError = "Enter a valid 10-digit mobile number"
            hasError = true
        }

        if (state.loginPin.length != 6) {
            pinError = "Enter your 6-digit secret PIN"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    loginMobileError = mobileError,
                    loginPinError = pinError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, loginGeneralError = null) }

        viewModelScope.launch {
            try {
                val result = authRepository.loginWithMobile(state.loginMobile, state.loginPin)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginGeneralError = null,
                            successMessage = "Logged in successfully as ${result.getOrNull()?.inGameName}"
                        )
                    }
                } else {
                    val errMsg = result.exceptionOrNull()?.message ?: "Login failed. Please verify credentials."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginGeneralError = errMsg
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginGeneralError = t.message ?: "Login failed. Please verify credentials."
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        var hasError = false
        var fullNameError: String? = null
        var mobileError: String? = null
        var ignError: String? = null
        var uidError: String? = null
        var pinError: String? = null

        if (state.regFullName.trim().length < 2) {
            fullNameError = "Enter your full name"
            hasError = true
        }

        if (state.regMobile.length != 10) {
            mobileError = "10-digit mobile number is required"
            hasError = true
        }

        if (state.regIgn.trim().length < 2) {
            ignError = "Enter your Player In-Game Name (IGN)"
            hasError = true
        }

        if (state.regInGameUid.trim().length < 6) {
            uidError = "Enter valid Player In-Game UID (minimum 6 digits)"
            hasError = true
        }

        if (state.regPin.length != 6) {
            pinError = "Set a 6-digit secret PIN"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    regFullNameError = fullNameError,
                    regMobileError = mobileError,
                    regIgnError = ignError,
                    regInGameUidError = uidError,
                    regPinError = pinError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, regGeneralError = null) }

        viewModelScope.launch {
            try {
                val result = authRepository.signUpWithMobile(
                    fullName = state.regFullName,
                    mobileNumber = state.regMobile,
                    inGameName = state.regIgn,
                    inGameUid = state.regInGameUid,
                    pin = state.regPin,
                    referralCode = state.regReferralCode
                )

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            regGeneralError = null,
                            successMessage = "Account registered! Welcome bonus of ₹10 added to your wallet."
                        )
                    }
                } else {
                    val errMsg = result.exceptionOrNull()?.message ?: "Registration failed. Try again."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            regGeneralError = errMsg
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        regGeneralError = t.message ?: "Registration failed. Try again."
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateProfile(inGameName: String, inGameUid: String) {
        authRepository.updateProfile(inGameName, inGameUid)
        _uiState.update { it.copy(successMessage = "Game profile updated successfully!") }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
