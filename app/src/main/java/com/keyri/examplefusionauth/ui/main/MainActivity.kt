package com.keyri.examplefusionauth.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.keyri.examplefusionauth.ui.credentials.CredentialsActivity.Companion.USER_ID_EXTRA_KEY
import com.keyri.examplefusionauth.ui.credentials.CredentialsActivity.Companion.PASSWORD_EXTRA_KEY
import com.keyri.examplefusionauth.databinding.ActivityMainBinding
import com.keyri.examplefusionauth.ui.credentials.CredentialsActivity
import com.keyrico.keyrisdk.Keyri
import com.keyrico.scanner.easyKeyriAuth
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val easyKeyriAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val text = if (it.resultCode == RESULT_OK) "Authenticated" else "Failed to authenticate"

            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }

    private val getCredentialsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val userId = it.data?.getStringExtra(USER_ID_EXTRA_KEY)
                val password = it.data?.getStringExtra(PASSWORD_EXTRA_KEY)

                viewModel.authenticate(checkNotNull(userId), password)
            }
        }

    private val viewModel by viewModels<MainViewModel>()

    private var authenticationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.bFusionAuth.setOnClickListener {
            authWithFusionAuth()
        }

        observeViewModel()
    }

    private fun authWithFusionAuth() {
        getCredentialsLauncher.launch(Intent(this, CredentialsActivity::class.java))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authResponseFlow.collect {
                    it?.takeIf { !authenticationStarted }?.let { authResponse ->
                        val email = authResponse.first
                        val keyri = Keyri(this@MainActivity, APP_KEY)

                        val payload = JSONObject().apply {
                            put("token", authResponse.second)
                            put("provider", "fusionauth:email_password") // Optional
                            put("timestamp", System.currentTimeMillis()) // Optional
                            put("associationKey", keyri.getAssociationKey(email)) // Optional
                            put(
                                "userSignature",
                                keyri.generateUserSignature(email, email)
                            ) // Optional
                        }.toString()

                        authenticationStarted = true

                        keyriAuth(email, payload)
                    }
                }
            }
        }
    }

    private fun keyriAuth(publicUserId: String?, payload: String) {
        easyKeyriAuth(this, easyKeyriAuthLauncher, APP_KEY, payload, publicUserId)
    }

    companion object {
        private const val APP_KEY = "IT7VrTQ0r4InzsvCNJpRCRpi1qzfgpaj"
    }
}
