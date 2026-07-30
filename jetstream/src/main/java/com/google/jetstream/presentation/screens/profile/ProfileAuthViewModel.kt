package com.google.jetstream.presentation.screens.profile

import androidx.lifecycle.ViewModel
import com.google.jetstream.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileAuthViewModel @Inject constructor(
    val authRepository: AuthRepository,
) : ViewModel() {
    val isLoggedIn = authRepository.isLoggedIn
    val currentUser = authRepository.currentUser

    fun logout() = authRepository.logout()
}
