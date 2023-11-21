package com.gigglegang.openedmaps

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var togglePasswordVisibility: ToggleButton
    private lateinit var toggleConfirmPasswordVisibility: ToggleButton
    private lateinit var btnReg: Button
    private lateinit var loginText : TextView

    //declare the instance of FB auth
    private lateinit var mAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.userEmail)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility)
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility)
        btnReg = findViewById(R.id.btnReg)
        loginText = findViewById(R.id.already_exist)

        //set the get instance for FB
        mAuth = FirebaseAuth.getInstance()

        btnReg.setOnClickListener(View.OnClickListener { view -> createUser(view) })

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

        // Set an OnCheckedChangeListener to toggle confirm password visibility
        toggleConfirmPasswordVisibility.setOnCheckedChangeListener { buttonView, isChecked ->
            val inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            confirmPasswordEditText.inputType = inputType

            // Move the cursor to the end of the input field
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)

        }
    }

    private fun createUser(view: View) {
        if (view.id == R.id.btnReg) {
            val email = emailEditText.text.toString().trim { it <= ' ' }
            val password = passwordEditText.text.toString().trim { it <= ' ' }
            val conPass = confirmPasswordEditText.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Username can't be blank", Toast.LENGTH_SHORT).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Password can't be blank", Toast.LENGTH_SHORT).show()
            }
            if (TextUtils.isEmpty(conPass)) {
                Toast.makeText(this, "Confirm password can't be blank", Toast.LENGTH_SHORT).show()
            }
            if (password == conPass) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            finish() // closes up the FB instance
                        } else {
                            Toast.makeText(this,"Failed to register",Toast.LENGTH_SHORT).show()
                        }
                    }
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun goToLoginPage(view: View) {
        // Navigate to the registration page
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
}