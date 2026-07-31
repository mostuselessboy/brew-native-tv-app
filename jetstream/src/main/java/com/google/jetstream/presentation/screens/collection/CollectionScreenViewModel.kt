package com.google.jetstream.presentation.screens.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.CollectionSectionDetails
import com.google.jetstream.data.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CollectionScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionScreenUiState>(CollectionScreenUiState.Loading)
    val uiState: StateFlow<CollectionScreenUiState> = _uiState.asStateFlow()

    init {
        val sectionId = savedStateHandle.get<String>(CollectionScreen.SectionIdBundleKey)
        if (sectionId.isNullOrBlank()) {
            _uiState.value = CollectionScreenUiState.Error
        } else {
            viewModelScope.launch {
                runCatching {
                    movieRepository.getCollectionSection(sectionId)
                }.onSuccess { details ->
                    _uiState.value = CollectionScreenUiState.Done(details)
                }.onFailure {
                    _uiState.value = CollectionScreenUiState.Error
                }
            }
        }
    }
}

sealed interface CollectionScreenUiState {
    data object Loading : CollectionScreenUiState
    data object Error : CollectionScreenUiState
    data class Done(val details: CollectionSectionDetails) : CollectionScreenUiState
}
