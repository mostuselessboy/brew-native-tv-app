package com.google.jetstream.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthRepository
import com.google.jetstream.data.auth.AuthResult
import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.auth.BrewUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountsAuthUiState(
    val isSignedIn: Boolean = false,
    val user: BrewUser? = null,
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    sessionStore: AuthSessionStore,
) : ViewModel() {

    val uiState: StateFlow<AccountsAuthUiState> = sessionStore.currentUser
        .map { user ->
            AccountsAuthUiState(
                isSignedIn = authRepository.isAuthenticated,
                user = user,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsAuthUiState(isSignedIn = authRepository.isAuthenticated),
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            authRepository.refreshAuthState()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
