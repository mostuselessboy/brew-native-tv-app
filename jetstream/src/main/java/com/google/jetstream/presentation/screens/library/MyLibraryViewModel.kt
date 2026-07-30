package com.google.jetstream.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.repositories.AuthRepository
import com.google.jetstream.data.repositories.LibraryRepository
import com.google.jetstream.data.repositories.MyLibraryContent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    val isLoggedIn = authRepository.isLoggedIn

    private val libraryContent = MutableStateFlow<MyLibraryUiState>(MyLibraryUiState.Loading)

    val uiState: StateFlow<MyLibraryUiState> = combine(
        authRepository.isLoggedIn,
        authRepository.currentUser,
        libraryContent,
    ) { loggedIn, user, library ->
        when {
            !loggedIn || user?.id == null -> MyLibraryUiState.SignInRequired
            library is MyLibraryUiState.Ready || library is MyLibraryUiState.Error -> library
            else -> library
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MyLibraryUiState.Loading,
    )

    init {
        viewModelScope.launch {
            combine(authRepository.isLoggedIn, authRepository.currentUser) { loggedIn, user ->
                loggedIn to user?.id
            }.collect { (loggedIn, userId) ->
                if (!loggedIn || userId == null) {
                    libraryContent.value = MyLibraryUiState.SignInRequired
                    return@collect
                }
                libraryContent.value = MyLibraryUiState.Loading
                libraryContent.value = runCatching {
                    libraryRepository.getMyLibrary(userId)
                }.fold(
                    onSuccess = { MyLibraryUiState.Ready(it) },
                    onFailure = {
                        MyLibraryUiState.Error(it.message ?: "Failed to load library")
                    },
                )
            }
        }
    }
}

sealed interface MyLibraryUiState {
    data object Loading : MyLibraryUiState
    data object SignInRequired : MyLibraryUiState
    data class Ready(val content: MyLibraryContent) : MyLibraryUiState
    data class Error(val message: String) : MyLibraryUiState
}
