package com.udacity.project4.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.ui.authentication.AuthenticationViewModel.AuthenticationState.AUTHENTICATED
import com.udacity.project4.ui.authentication.AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED

class AuthenticationViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED,
        UNAUTHENTICATED,
        INVALID_AUTHENTICATION;
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) AUTHENTICATED else UNAUTHENTICATED
    }


}