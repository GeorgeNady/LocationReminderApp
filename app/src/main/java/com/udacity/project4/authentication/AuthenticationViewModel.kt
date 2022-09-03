package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.authentication.AuthenticationViewModel.AuthenticationState.AUTHENTICATED
import com.udacity.project4.authentication.AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED

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