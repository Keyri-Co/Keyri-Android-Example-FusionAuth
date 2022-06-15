package com.keyri.examplefusionauth.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inversoft.error.Errors
import com.inversoft.rest.ClientResponse
import com.keyri.examplefusionauth.BuildConfig
import io.fusionauth.client.FusionAuthClient
import io.fusionauth.client.LambdaDelegate
import io.fusionauth.domain.api.LoginRequest
import io.fusionauth.domain.api.LoginResponse
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _authResponseFlow = MutableStateFlow<Pair<String, String>?>(null)

    val authResponseFlow: StateFlow<Pair<String, String>?>
        get() = _authResponseFlow

    fun authenticate(userId: String, password: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val client = FusionAuthClient(BuildConfig.API_KEY, LOCALHOST)

            val applicationId = UUID.fromString(BuildConfig.APPLICATION_ID)

            val delegate = LambdaDelegate(
                client,
                { handleResult((it.successResponse as LoginResponse)) },
                { handleError(it) }
            )

            val loginRequest = LoginRequest(applicationId, userId, password)

            val execution = delegate.execute { client.login(loginRequest) }
        }
    }

    private fun handleResult(loginResponse: LoginResponse) {
        _authResponseFlow.value = loginResponse.user.email to loginResponse.token
    }

    private fun <T, U> handleError(clientResponse: ClientResponse<T, U>) {
        if (clientResponse.exception != null) {
            Log.e("RESULT - Error", "${clientResponse.exception}")
        } else if (clientResponse.errorResponse != null && clientResponse.errorResponse is Errors) {
            Log.e("RESULT - Error", "${clientResponse.errorResponse}")
        }
    }

    companion object {
        private const val LOCALHOST = "http://10.0.2.2:9011" // Later your server URL
    }
}
