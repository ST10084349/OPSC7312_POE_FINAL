package com.gigglegang.openedmaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    //variables
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //typecasting
        auth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.editTextEmail)
        resetButton = findViewById(R.id.resetButton)


    }

    fun onResetPasswordClick(view: View) {
        val email = emailEditText.text.toString()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email sent, handle success
                    Toast.makeText(this,"Password Reset Request Successful", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle password reset failure
                    Toast.makeText(this,"Password Reset Request Unsuccessful", Toast.LENGTH_SHORT).show()
                }
            }
        }
}