package com.project.fingerprint.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
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
    private lateinit var adapter:ListAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    private lateinit var promptInfoBuilder: PromptInfo.Builder
    private lateinit var kgm: KeyguardManager

    lateinit var binding: ActivityMainBinding

    companion object {
        var isAuthentic = false
    }




    override fun onStart() {



        sharedPreferences = getSharedPreferences("key", Context.MODE_PRIVATE)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = getString(R.string.contacts)

        setUpRecyclerView()

        bioMetricSetup()

        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityMainBinding.inflate(layoutInflater)
        binding.root.visibility=View.GONE
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            withContext(Dispatchers.Main) {
                Log.i("onResume: ", "MainActivity")
                if (sharedPreferences.getBoolean("bool", false) && kgm.isDeviceSecure) {
                    if (isAuthentic) {
                        binding.root.visibility = View.VISIBLE
                    } else {
                        binding.root.visibility = View.GONE
                        biometricPrompt.authenticate(promptInfo)
                    }
                } else {
                    binding.root.visibility = View.VISIBLE
                    sharedPreferences.edit().putBoolean("bool", false).apply()
                    Toast.makeText(this@MainActivity, "To enable App Lock , click on Settings at the top", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun bioMetricSetup() {

        promptInfoBuilder = PromptInfo.Builder()
            .setTitle(getString(R.string.tittle)).setSubtitle(getString(R.string.sub_tittle))
            .setDescription(getString(R.string.sub_tittle)).setConfirmationRequired(false)

        val executor = ContextCompat.getMainExecutor(this@MainActivity)
        kgm = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            promptInfo = promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK).build()
        } else if (kgm.isDeviceSecure) {
            promptInfo = promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL).build()
        }

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {

                Toast.makeText(this@MainActivity, "Login Success ${result.authenticationType}", Toast.LENGTH_SHORT).show()
                binding.root.visibility = View.VISIBLE
                isAuthentic = true
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(this@MainActivity, "Login Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {

                if (errString.contains(getString(R.string.no_enrollement))) {
                    Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
                } else if (errString.contains(getString(R.string.no_pin_password_set))) {
                    sharedPreferences.edit().putBoolean("bool", false).apply()
                    binding.root.visibility = View.GONE
                }
                else if (errString.contains("Authentication cancelled") || errString.contains("Authentication canceled by user")){
                    finishAffinity()
                    exitProcess(0)
                }
                else{
                    Toast.makeText(this@MainActivity, "Login Error $errString", Toast.LENGTH_SHORT).show()
                }

            }
        })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchView = menu!!.findItem(R.id.action_search).actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                tempList.clear()
                val searchText = p0.toString().lowercase()
                if (searchText.isNotEmpty()) {
                    newList.forEach {
                        if (it.lowercase().contains(searchText)) {
                            tempList.add(it)
                        }
                    }
                  adapter.filterList(tempList)

                } else {
                    tempList.clear()
                    tempList.addAll(newList)
                    adapter.filterList(tempList)
                }
                return false
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this@MainActivity, EnableFingerPrintActivity::class.java))
                true
            }
            R.id.action_search -> {
                true
            }
            else -> {
                return false
            }
        }
    }
}