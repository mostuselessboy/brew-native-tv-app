package com.google.jetstream.presentation.screens.profile

import androidx.lifecycle.ViewModel
import com.google.jetstream.data.auth.AuthSessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TvProfile(
    val id: String,
    val name: String,
) {
    val initial: String
        get() = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "B"
}

data class UserSelectionUiState(
    val profiles: List<TvProfile> = emptyList(),
)

@HiltViewModel
class UserSelectionViewModel @Inject constructor(
    authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSelectionUiState())
    val uiState: StateFlow<UserSelectionUiState> = _uiState.asStateFlow()

    init {
        val displayName = authSessionStore.currentUser.value?.displayName
            ?: authSessionStore.cachedUserName?.takeIf { it.isNotBlank() }
            ?: "Guest"
        _uiState.value = UserSelectionUiState(
            profiles = listOf(
                TvProfile(
                    id = authSessionStore.currentUserId()?.toString() ?: "guest",
                    name = displayName,
                ),
            ),
        )
    }
}
