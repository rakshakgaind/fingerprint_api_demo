package com.project.fingerprint.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.project.fingerprint.R
import com.project.fingerprint.base.BaseActivity
import com.project.fingerprint.databinding.ActivityEnableFingerPrintBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class EnableFingerPrintActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityEnableFingerPrintBinding
    private lateinit var kgm: KeyguardManager

    private val authenticationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        sharedPreferences.edit().putBoolean("bool", true).apply()

        if (kgm.isDeviceSecure) {
            sharedPreferences.edit().putBoolean("bool", true)
            binding.switchView.isChecked=true
        } else {
            sharedPreferences.edit().putBoolean("bool", false).apply()
            binding.switchView.isChecked = false
            Toast.makeText(this@EnableFingerPrintActivity, getString(R.string.lock_not_set), Toast.LENGTH_SHORT).show()
        }

    }


    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var promptInfoBuilder: BiometricPrompt.PromptInfo.Builder

    override fun onStart() {
        super.onStart()
        binding = ActivityEnableFingerPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("key", Context.MODE_PRIVATE)

        binding.switchView.isChecked = sharedPreferences.getBoolean("bool", false)

        biometricSetup()

        binding.switchView.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                checkBiometricAvailable()

            } else {
                sharedPreferences.edit().putBoolean("bool", false).apply()
            }
        }

    }

    private fun biometricSetup() {
        promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.tittle)).setSubtitle(getString(R.string.sub_tittle))
            .setDescription(getString(R.string.desc)).setConfirmationRequired(false)

        val executor = ContextCompat.getMainExecutor(this@EnableFingerPrintActivity)
        val kgm = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            promptInfo =
                promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build()
        } else if (kgm.isDeviceSecure) {
            promptInfo = promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or DEVICE_CREDENTIAL).build()
        }

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Toast.makeText(this@EnableFingerPrintActivity, "Login Success ${result.authenticationType}", Toast.LENGTH_SHORT).show()
                binding.root.visibility = View.VISIBLE
                MainActivity.isAuthentic = true


            }

            override fun onAuthenticationFailed() {
                Toast.makeText(this@EnableFingerPrintActivity, getString(R.string.failed), Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(this@EnableFingerPrintActivity, "Login Error $errString", Toast.LENGTH_SHORT).show()
                if (errString.contains(getString(R.string.no_enrollement))) {
                    Toast.makeText(this@EnableFingerPrintActivity, "$errString", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun checkBiometricAvailable() {
        kgm = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val biometricManager = BiometricManager.from(this)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            when (biometricManager.canAuthenticate(
                BIOMETRIC_STRONG
                        or DEVICE_CREDENTIAL
            )) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(this, getString(R.string.enrollement), Toast.LENGTH_SHORT).show()
                    sharedPreferences.edit().putBoolean("bool", false).apply()
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }

                    authenticationLauncher.launch(enrollIntent)
                }
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    sharedPreferences.edit().putBoolean("bool", true).apply()
                    Toast.makeText(this@EnableFingerPrintActivity, getString(R.string.success_message), Toast.LENGTH_SHORT).show()

                }

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    Toast.makeText(this@EnableFingerPrintActivity, "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    Toast.makeText(this@EnableFingerPrintActivity, "BIOMETRIC_ERROR_UNSUPPORTED", Toast.LENGTH_SHORT).show()
                }
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    Toast.makeText(this@EnableFingerPrintActivity, "BIOMETRIC_STATUS_UNKNOWN", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (kgm.isDeviceSecure) {
                sharedPreferences.edit().putBoolean("bool", true).apply()

            } else {
                authenticationLauncher.launch(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
            }
        } else {
            sharedPreferences.edit().putBoolean("bool", false).apply()
            Toast.makeText(this, getString(R.string.enrollement), Toast.LENGTH_SHORT).show()
            val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            authenticationLauncher.launch(enrollIntent)

        }

    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            withContext(Dispatchers.Main) {

                if (sharedPreferences.getBoolean("bool", false)) {
                    if (MainActivity.isAuthentic) {
                        binding.root.visibility = View.VISIBLE
                    } else {
                        binding.root.visibility = View.GONE
                        biometricPrompt.authenticate(promptInfo)
                    }

                } else {
                    binding.root.visibility = View.VISIBLE
                }
            }
        }

    }

}