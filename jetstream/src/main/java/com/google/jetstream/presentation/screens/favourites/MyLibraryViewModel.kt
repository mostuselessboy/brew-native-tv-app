package com.google.jetstream.presentation.screens.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.auth.AuthSessionStore
import com.google.jetstream.data.entities.LibraryItem
import com.google.jetstream.data.entities.LibraryShelf
import com.google.jetstream.data.entities.MyLibraryPage
import com.google.jetstream.data.repositories.LibraryRepository
import com.google.jetstream.data.util.resolveUserAvatarDisplayUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyLibraryUiState>(MyLibraryUiState.Loading)
    val uiState: StateFlow<MyLibraryUiState> = _uiState.asStateFlow()

    private var cachedPage: MyLibraryPage? = null

    init {
        refresh()
    }

    fun refresh() {
        if (!authSessionStore.isAuthenticated()) {
            _uiState.value = MyLibraryUiState.Guest
            return
        }
        val userId = authSessionStore.currentUserId()
        if (userId == null || userId <= 0) {
            _uiState.value = MyLibraryUiState.Guest
            return
        }

        val user = authSessionStore.currentUser.value
        val userName = user?.displayName
        val avatarUrl = resolveUserAvatarDisplayUrl(user, 168)

        libraryRepository.peekMyLibrary(userId)?.let { page ->
            cachedPage = page
            _uiState.value = page.toUiState(userName, avatarUrl)
        } ?: run {
            _uiState.value = MyLibraryUiState.Loading
        }

        viewModelScope.launch {
            libraryRepository.fetchMyLibrary(userId).fold(
                onSuccess = { page ->
                    cachedPage = page
                    val latestUser = authSessionStore.currentUser.value
                    _uiState.value = page.toUiState(
                        userName = latestUser?.displayName,
                        avatarUrl = resolveUserAvatarDisplayUrl(latestUser, 168),
                    )
                },
                onFailure = {
                    if (_uiState.value is MyLibraryUiState.Loading) {
                        _uiState.value = MyLibraryUiState.Error(it.message ?: "Couldn't load library")
                    }
                },
            )
        }
    }

    fun loadMoreShelf(shelf: LibraryShelf) {
        if (!shelf.hasMore || shelf.id.apiId.isBlank()) return
        val userId = authSessionStore.currentUserId() ?: return
        val current = _uiState.value as? MyLibraryUiState.Ready ?: return
        if (current.loadingShelfId == shelf.id) return

        _uiState.value = current.copy(loadingShelfId = shelf.id)
        viewModelScope.launch {
            libraryRepository.loadMoreShelf(
                userId = userId,
                shelfId = shelf.id.apiId,
                offset = shelf.offset,
            ).fold(
                onSuccess = { newItems ->
                    val merged = mergeShelfItems(current.page, shelf.id, newItems)
                    cachedPage = merged
                    _uiState.value = MyLibraryUiState.Ready(
                        page = merged,
                        userName = current.userName,
                        avatarUrl = current.avatarUrl,
                        loadingShelfId = null,
                    )
                },
                onFailure = {
                    _uiState.value = current.copy(loadingShelfId = null)
                },
            )
        }
    }

    private fun MyLibraryPage.toUiState(userName: String?, avatarUrl: String?): MyLibraryUiState =
        when {
            isEmpty -> MyLibraryUiState.Empty(userName, avatarUrl)
            else -> MyLibraryUiState.Ready(this, userName, avatarUrl)
        }

    private fun mergeShelfItems(
        page: MyLibraryPage,
        shelfId: com.google.jetstream.data.entities.LibraryShelfId,
        newItems: List<LibraryItem>,
    ): MyLibraryPage {
        val shelves = page.shelves.map { shelf ->
            if (shelf.id != shelfId) return@map shelf
            val combined = (shelf.items + newItems).distinctBy { it.movieId }
            shelf.copy(
                items = combined,
                offset = shelf.offset + newItems.size,
                hasMore = newItems.isNotEmpty() && combined.size < shelf.total,
            )
        }
        return page.copy(shelves = shelves)
    }
}

sealed interface MyLibraryUiState {
    data object Loading : MyLibraryUiState
    data object Guest : MyLibraryUiState
    data class Empty(val userName: String?, val avatarUrl: String? = null) : MyLibraryUiState
    data class Error(val message: String) : MyLibraryUiState
    data class Ready(
        val page: MyLibraryPage,
        val userName: String?,
        val avatarUrl: String? = null,
        val loadingShelfId: com.google.jetstream.data.entities.LibraryShelfId? = null,
    ) : MyLibraryUiState
}
