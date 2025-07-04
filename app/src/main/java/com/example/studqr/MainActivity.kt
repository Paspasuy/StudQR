package com.example.studqr

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.example.studqr.api.AttentifyClient
import com.example.studqr.api.ConnectionException
import com.example.studqr.api.Lesson
import com.example.studqr.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import io.ktor.client.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.mutableMapOf


object SessionManager {
    val scheduleDeferred = CompletableDeferred<AttentifyClient>()
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    var currentLanguage = "en"
    var lessonCache = mutableMapOf<String, List<Lesson>>()
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    internal var attentifyClient = AttentifyClient()

    var barcodeLauncher = registerForActivityResult<ScanOptions?, ScanIntentResult?>(
        ScanContract(), ActivityResultCallback { result: ScanIntentResult? ->
            if (result!!.getContents() == null) {
                Toast.makeText(this@MainActivity, getString(R.string.cancelled), Toast.LENGTH_LONG)
                    .show()
            } else {
                val resultString = result.getContents()

                SessionManager.scope.launch {
                    var result = R.string.checkin_success
                    try {
                        attentifyClient.checkIn(resultString)
                    } catch (e: ConnectionException) {
                        Log.e("BaseClient", e.toString())
                        if (e.statusCode == 409) {
                            result = R.string.checkin_double
                        } else {
                            result = R.string.checkin_failure
                        }
                    } catch (e: ConnectTimeoutException) {
                        result = R.string.checkin_failure
                    }
                    Snackbar.make(binding.fab, result, Snackbar.LENGTH_LONG).setAnchorView(R.id.fab)
                        .show()
                }
            }
        })

    fun scanCode() {
        var options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setBeepEnabled(false)
        options.setTorchEnabled(false)
        options.setOrientationLocked(true)

        options.setPrompt(getString(R.string.scan_qr));

        barcodeLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SessionManager.currentLanguage = resources.configuration.locales.get(0).language

        super.onCreate(savedInstanceState)

        val login = intent.getStringExtra("login")!!
        val password = intent.getStringExtra("password")!!

        SessionManager.scope.launch {
            try {
                attentifyClient.login(login, password)
                SessionManager.scheduleDeferred.complete(attentifyClient)

                var me = attentifyClient.getMe()
                binding.toolbar.title = me.email

                Log.e("Login: ", me.email)
                Log.e("ID: ", me.id.toString())
            } catch (e: ConnectionException) {
                Snackbar.make(binding.fab, R.string.fetch_failure, Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab).show()
                Log.e("BaseClient", e.toString())
                SessionManager.scheduleDeferred.completeExceptionally(e)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<ComposeView>(R.id.compose_view).setContent {
            ScheduleComponent()
        }

        setSupportActionBar(binding.toolbar)

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
            R.id.action_english -> {
                setAppLocale2("en", this)
                finish();
                startActivity(getIntent());
                true
            }
            R.id.action_russian -> {
                setAppLocale2("ru", this)
                finish();
                startActivity(getIntent());
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}