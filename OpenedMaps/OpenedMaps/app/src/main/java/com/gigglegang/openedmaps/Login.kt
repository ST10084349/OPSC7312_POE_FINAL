package com.gigglegang.openedmaps

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var resetPasswordText: TextView
    private lateinit var togglePasswordVisibility: ToggleButton
    private lateinit var loginButton: Button
    private lateinit var mAuth: FirebaseAuth


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.passwordEditText)
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility)
        loginButton = findViewById(R.id.btnLogin)
        resetPasswordText = findViewById(R.id.resetPass)

        loginButton.setOnClickListener {
            loginUser()
        }

        // Set an OnCheckedChangeListener to toggle password visibility
        togglePasswordVisibility.setOnCheckedChangeListener { buttonView, isChecked ->
            val inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordEditText.inputType = inputType
            // Move the cursor to the end of the input field
            passwordEditText.setSelection(passwordEditText.text.length)
        }
    }

    fun goToRegisterPage(view: View) {
        // Navigate to the registration page
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    fun goToResetPassPage(view: View) {
        // Navigate to the registration page
        val intent = Intent(this, ForgotPassword::class.java)
        startActivity(intent)
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address!", Toast.LENGTH_SHORT).show()
            emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password!", Toast.LENGTH_SHORT).show()
            passwordEditText.requestFocus()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    emailEditText.setText("")
                    passwordEditText.setText("")
                    emailEditText.requestFocus()
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this,
                        "Login Unsuccessful! Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    emailEditText.setText("")
                    passwordEditText.setText("")
                    emailEditText.requestFocus()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
}