# Overview

This module contains example of implementation [Keyri](https://keyri.com) with FusionAuth.

## Contents

* [Requirements](#Requirements)
* [Permissions](#Permissions)
* [Keyri Integration](#Keyri-Integration)
* [FusionAuth Integration](#FusionAuth-Integration)
* [Authentication](#Authentication)

## Requirements

* Android API level 23 or higher
* AndroidX compatibility
* Kotlin coroutines compatibility

Note: Your app does not have to be written in kotlin to integrate this SDK, but must be able to
depend on kotlin functionality.

## Permissions

Open your app's `AndroidManifest.xml` file and add the following permission:

```xml

<uses-permission android:name="android.permission.INTERNET" />
```

## Keyri Integration

* Add the JitPack repository to your root build.gradle file:

```groovy
allprojects {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```

* Add SDK dependency to your build.gradle file and sync project:

```kotlin
dependencies {
    // ...
    implementation("com.github.Keyri-Co.keyri-android-whitelabel-sdk:keyrisdk:$latestKeyriVersion")
    implementation("com.github.Keyri-Co.keyri-android-whitelabel-sdk:scanner:$latestKeyriVersion")
}
```

## FusionAuth Integration

Follow this [5-Minute Setup Guide](https://fusionauth.io/docs/v1/tech/5-minute-setup-guide) to setup
and deploy FusionAuth project. You can
use [FusionAuth Java Client](https://fusionauth.io/docs/v1/tech/client-libraries/java) for easy
integration into your Android project:

```kotlin
dependencies {
    // ...
    implementation("io.fusionauth:fusionauth-java-client:1.20.0")
}
```

Create `FusionAuthClient` object with the provision of your *API key* and *base
URL* (`http://10.0.2.2:9011` equals `http://localhost:9011`). And execute login
request `LoginRequest` with passing *Application ID*, *UserId* and *password* (can be null).

```kotlin
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
```

## Authentication

Use `token` and `email` fields to create payload and user signature:

```kotlin
val email = authResponsePair.first
val keyri = Keyri()

val payload = JSONObject().apply {
    put("token", authResponsePair.second)
    put("provider", "fusionauth:email_password") // Optional
    put("timestamp", System.currentTimeMillis()) // Optional
    put("associationKey", keyri.getAssociationKey(email)) // Optional
    put("userSignature", keyri.getUserSignature(email, email)) // Optional
}.toString()

keyriAuth(email, payload)
```

Authenticate with Keyri. In the next showing `AuthWithScannerActivity` with providing
`publicUserId` and `payload`.

```kotlin
private val easyKeyriAuthLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Process authentication result
    }

private fun keyriAuth(publicUserId: String?, payload: String) {
    val intent = Intent(this, AuthWithScannerActivity::class.java).apply {
        putExtra(AuthWithScannerActivity.APP_KEY, BuildConfig.APP_KEY)
        putExtra(AuthWithScannerActivity.PUBLIC_USER_ID, publicUserId)
        putExtra(AuthWithScannerActivity.PAYLOAD, payload)
    }

    easyKeyriAuthLauncher.launch(intent)
}
```
