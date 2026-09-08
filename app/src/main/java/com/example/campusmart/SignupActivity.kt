package com.example.campusmart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmart.data.repository.AuthRepository
import com.example.campusmart.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupButton.setOnClickListener {
            handleSignup()
        }

        binding.goToLogin.setOnClickListener {
            finish() // just close signup, return to login below it
        }
    }

    private fun handleSignup() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        binding.signupProgress.visibility = android.view.View.VISIBLE
        binding.signupButton.isEnabled = false

        lifecycleScope.launch {
            val result = authRepository.signup(name, email, password)

            binding.signupProgress.visibility = android.view.View.GONE
            binding.signupButton.isEnabled = true

            if (result.isSuccess) {
                Toast.makeText(this@SignupActivity, "Account created!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this@SignupActivity,
                    "Signup failed: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}