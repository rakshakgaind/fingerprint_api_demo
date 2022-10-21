package com.project.fingerprint.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.project.fingerprint.R
import com.project.fingerprint.adapter.ListAdapter
import com.project.fingerprint.base.BaseActivity
import com.project.fingerprint.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    private var newList = ArrayList<String>()
    private var tempList = ArrayList<String>()
    private lateinit var adapter: ListAdapter

    override lateinit var biometricPrompt: BiometricPrompt
    override lateinit var promptInfo: PromptInfo
    override lateinit var promptInfoBuilder: PromptInfo.Builder

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    companion object {
        var isAuthentic = false
    }

    /**
     * Callback which detects user setup any Auth or security option or not
     */
    override var authenticationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        sharePrefs.saveData(true)
        if (kgm.isDeviceSecure) {
            sharePrefs.saveData(true)
            binding.switchView.isChecked = true
        } else {
            sharePrefs.saveData(false)
            binding.switchView.isChecked = false
            Toast.makeText(this@MainActivity, getString(R.string.lock_not_set), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Activity lifecycle functions
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        listeners()
    }

    override fun onStart() {
        setUpBiometricPrompt()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            withContext(Dispatchers.Main) {
                if (sharePrefs.getData() && kgm.isDeviceSecure) {
                    if (isAuthentic) {
                        binding.root.visibility = View.VISIBLE
                    } else {
                        binding.root.visibility = View.GONE
                        biometricPrompt.authenticate(promptInfo)
                    }
                } else {
                    binding.root.visibility = View.VISIBLE
                    sharePrefs.saveData(false)
                    Toast.makeText(this@MainActivity, getString(R.string.instruction_for_app_lock), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initView() {
        binding.root.visibility = View.GONE
        binding.switchView.isChecked = sharePrefs.getData()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = getString(R.string.contacts)
        setUpRecyclerView()
    }

    private fun listeners() {
        binding.switchView.setOnCheckedChangeListener { _, p1 ->
            if (p1) checkBiometricAvailable()
            else sharePrefs.saveData(false)
        }
    }

    private fun setUpRecyclerView() {
        addDataToList()
        adapter = ListAdapter()
        binding.recyclerview.adapter = adapter
        adapter.setData(tempList)
    }

    private fun addDataToList() {
        for (i in 1..50) {
            newList.add("Amit Kumar")
            newList.add("Shubham Kumar")
            newList.add("Anurag Kashyap")
            newList.add("Manish Kumar")
            newList.add("Aloo Arjun")
            newList.add("Akshay Kumar")
        }
        tempList.addAll(newList)
    }

    override fun onAuthenticationErrors(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationErrors(errorCode, errString)
        if (errString.contains(getString(R.string.no_enrollement))) {
            Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
        } else if (errString.contains(getString(R.string.no_pin_password_set))) {
            sharePrefs.saveData(false)
            binding.root.visibility = View.GONE
        } else if (errString.contains("Authentication cancelled") || errString.contains("Authentication canceled by user")) {
            finishAffinity()
            exitProcess(0)
        } else {
            Toast.makeText(this@MainActivity, "Login Error $errString", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
        super.onSuccess(result)
        Toast.makeText(this@MainActivity, "Login Success ${result.authenticationType}", Toast.LENGTH_SHORT).show()
        binding.root.visibility = View.VISIBLE
        isAuthentic = true
    }
}