package com.example.studqr

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.studqr.databinding.ActivityLoginBinding
import java.util.Locale

class LoginActivity : ComponentActivity() {
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
//        setAppLocale2("ru", this)
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
