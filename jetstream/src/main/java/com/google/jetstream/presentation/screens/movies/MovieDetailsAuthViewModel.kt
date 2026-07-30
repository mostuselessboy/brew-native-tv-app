package com.google.jetstream.presentation.screens.movies

import androidx.lifecycle.ViewModel
import com.google.jetstream.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MovieDetailsAuthViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val isLoggedIn = authRepository.isLoggedIn
}
