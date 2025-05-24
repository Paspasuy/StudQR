package com.example.studqr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.studqr.databinding.ActivityLoginBinding
import com.example.studqr.databinding.ActivityMainBinding
import com.example.studqr.ui.theme.StudQRTheme

class LoginActivity : ComponentActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener { view ->
            val login = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextNumberPassword.text.toString()
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("password", password)
            startActivity(intent)
        }

    }
}
