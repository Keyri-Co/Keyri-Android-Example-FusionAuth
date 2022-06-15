package com.keyri.examplefusionauth.ui.credentials

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.keyri.examplefusionauth.databinding.ActivityCredentialsBinding

class CredentialsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCredentialsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityCredentialsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        with(binding) {
            bPassCredentials.setOnClickListener {
                val userId = etUserId.text.toString()
                val password = etPassword.text.toString()

                if (userId.isNotEmpty()) {
                    val intent = Intent().apply {
                        putExtra(USER_ID_EXTRA_KEY, userId)
                        putExtra(PASSWORD_EXTRA_KEY, password.takeIf { it.isNotEmpty() })
                    }

                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@CredentialsActivity,
                        "Email and password must not be empty",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        const val USER_ID_EXTRA_KEY = "USER_ID_EXTRA_KEY"
        const val PASSWORD_EXTRA_KEY = "PASSWORD_EXTRA_KEY"
    }
}
