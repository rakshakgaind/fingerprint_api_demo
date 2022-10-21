package com.project.fingerprint.base

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.project.fingerprint.R
import com.project.fingerprint.util.SharePrefs

/**
 * Base Activity if we've any common commands or functions
 * Like permissions
 */

abstract class BaseActivity : AppCompatActivity() {
    private val biometricManager by lazy { BiometricManager.from(this) }
    private val executor by lazy { ContextCompat.getMainExecutor(this@BaseActivity) }
    protected val kgm by lazy { getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    protected val sharePrefs = SharePrefs()

    abstract var authenticationLauncher: ActivityResultLauncher<Intent>
    abstract var biometricPrompt: BiometricPrompt
    abstract var promptInfo: BiometricPrompt.PromptInfo
    abstract var promptInfoBuilder: BiometricPrompt.PromptInfo.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBiometricPrompt()
    }

    /**
     * Detects all available security options
     * Ex: Face-Detection, Biometric, Device credentials like Pattern and Pin options
     */
    fun checkBiometricAvailable() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(this, getString(R.string.enrollement), Toast.LENGTH_SHORT).show()
                    sharePrefs.saveData(false)
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    }
                    authenticationLauncher.launch(enrollIntent)
                }

                BiometricManager.BIOMETRIC_SUCCESS -> {
                    sharePrefs.saveData(true)
                    Toast.makeText(this@BaseActivity, getString(R.string.success_message), Toast.LENGTH_SHORT).show()
                }

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    Toast.makeText(this@BaseActivity, "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED", Toast.LENGTH_SHORT).show()
                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    Toast.makeText(this@BaseActivity, "BIOMETRIC_ERROR_UNSUPPORTED", Toast.LENGTH_SHORT).show()
                }

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    Toast.makeText(this@BaseActivity, "BIOMETRIC_STATUS_UNKNOWN", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (kgm.isDeviceSecure) {
                sharePrefs.saveData(true)
            } else {
                authenticationLauncher.launch(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
            }
        } else {
            sharePrefs.saveData(false)
            Toast.makeText(this, getString(R.string.enrollement), Toast.LENGTH_SHORT).show()
            val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            authenticationLauncher.launch(enrollIntent)
        }
    }

    /**
     * Set any security option
     * Ex: Face-Detection, Biometric, Device credentials like Pattern and Pin option
     */
    protected fun setUpBiometricPrompt() {
        promptInfoBuilder = BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.tittle)).setSubtitle(getString(R.string.sub_tittle)).setDescription(getString(R.string.sub_tittle))
            .setConfirmationRequired(false)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            promptInfo = promptInfoBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
            ).build()
        } else if (kgm.isDeviceSecure) {
            promptInfo = promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build()
        }

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result)
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(this@BaseActivity, "Login Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onAuthenticationErrors(errorCode, errString)
            }
        })
    }

    open fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
    }

    open fun onAuthenticationErrors(errorCode: Int, errString: CharSequence) {
    }
}