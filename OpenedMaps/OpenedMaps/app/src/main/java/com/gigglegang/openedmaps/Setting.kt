package com.gigglegang.openedmaps

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Setting : AppCompatActivity() {

    private lateinit var unitsToggle: Switch
    private lateinit var maxDistanceSlider: SeekBar
    private lateinit var distanceTextView: TextView
    private lateinit var unitLabelTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var database: DatabaseReference

    data class SettingsModel(val isMetric: Boolean = true, val maxDistance: Int = 50)


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        unitsToggle = findViewById(R.id.unitsToggle)
        maxDistanceSlider = findViewById(R.id.maxDistanceSlider)
        distanceTextView = findViewById(R.id.distanceTextView)
        unitLabelTextView = findViewById(R.id.unitLabelTextView)
        logoutButton = findViewById(R.id.btnLogout)
        saveButton = findViewById(R.id.button1)
        cancelButton = findViewById(R.id.button2)
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Load saved settings
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isMetric = sharedPreferences.getBoolean("isMetric", true)
        val maxDistance = sharedPreferences.getInt("maxDistance", 50)

        // Set initial values for UI components based on saved settings and toggle state
        unitsToggle.isChecked = isMetric
        maxDistanceSlider.progress = maxDistance
        updateDistanceTextView(maxDistance, isMetric)
        updateUnitLabel(isMetric)

        unitsToggle.setOnCheckedChangeListener { _, isChecked ->
            // Update text when toggle state changes
            updateUnitLabel(isChecked)
            // Update distance text based on the new unit system
            updateDistanceTextView(maxDistanceSlider.progress, isChecked)
        }

        // Set up SeekBar change listener
        maxDistanceSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the distanceTextView based on the selected unit system
                updateDistanceTextView(progress, unitsToggle.isChecked)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle tracking touch if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Handle stopping touch if needed
            }
        })

        saveButton.setOnClickListener {
            // Save settings
            val editor = sharedPreferences.edit()
            editor.putBoolean("isMetric", unitsToggle.isChecked)
            editor.putInt("maxDistance", maxDistanceSlider.progress)
            editor.apply()

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish() // Close the settings activity
        }

        saveButton.setOnClickListener {
            val isMetric = unitsToggle.isChecked
            val maxDistance = maxDistanceSlider.progress

            val userData = SettingsModel(isMetric, maxDistance)

            database.setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Settings saved to Firebase", Toast.LENGTH_SHORT).show()
                    finish() // Close the settings activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
        }

        cancelButton.setOnClickListener {
            // Do nothing and close the settings activity
            finish()
        }

        logoutButton.setOnClickListener {
            val warningMessage = "Are you sure you want to log out?"

            val snackbar = Snackbar.make(it, warningMessage, Snackbar.LENGTH_LONG)
            snackbar.setAction("Confirm") {
                // Perform logout action
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    private fun updateDistanceTextView(progress: Int, isMetric: Boolean) {
        val distance = if (isMetric) {
            // Convert progress to kilometers if metric system is selected
            "$progress km"
        } else {
            // Convert progress to miles if imperial system is selected
            // 1 kilometer is approximately equal to 0.621371 miles
            val miles = (progress * 0.621371).toInt()
            "$miles miles"
        }
        // Update the distanceTextView based on the selected unit system
        distanceTextView.text = "Max Distance: $distance"
    }

    private fun updateUnitLabel(isMetric: Boolean) {
        val unitLabel = if (isMetric) {
            "Kilometers"
        } else {
            "Miles"
        }
        unitLabelTextView.text = "Selected Unit: $unitLabel"
    }
}