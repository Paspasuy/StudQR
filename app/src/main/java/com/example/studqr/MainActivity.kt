package com.example.studqr

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.studqr.api.AttentifyClient
import com.example.studqr.api.ConnectionException
import com.example.studqr.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var attentifyClient = AttentifyClient()

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    var barcodeLauncher = registerForActivityResult<ScanOptions?, ScanIntentResult?>(
        ScanContract(),
        ActivityResultCallback { result: ScanIntentResult? ->
            if (result!!.getContents() == null) {
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Scanned: " + result.getContents(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    fun scanCode() {
        var options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setBeepEnabled(false)
        options.setTorchEnabled(false)
        options.setOrientationLocked(true)

        options.setPrompt("Отсканируйте QR чтобы отметиться");

        barcodeLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val login = intent.getStringExtra("login")!!
        val password = intent.getStringExtra("password")!!

        scope.launch {
            try {
//                attentifyClient.login("teacher2@example.com", "teacherpassword")
                attentifyClient.login(login, password)

//                var schedule = attentifyClient.getScheduleDay("2025-05-20")
//                Log.e("SCHEDULE", schedule.toString())
                var me = attentifyClient.getMe()

                binding.toolbar.title = me.email
                Log.e("Login: ", me.email)
                Log.e("ID: ", me.id.toString())
            } catch (e: ConnectionException) {
                Snackbar.make(binding.fab, R.string.fetch_failure, Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab).show()

                Log.e("BaseClient", e.toString())

            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            scanCode()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}