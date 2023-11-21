package com.gigglegang.openedmaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.material.snackbar.Snackbar

class Home : AppCompatActivity() {

    private lateinit var cardMap: CardView
    private lateinit var cardSet: CardView
    private lateinit var cardBird: CardView
    private lateinit var cardAbout: CardView
    private lateinit var cardLogOut: CardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        cardMap = findViewById(R.id.cardMap) // Replace R.id.cardMap with your actual ID
        cardSet = findViewById(R.id.cardSet) // Replace R.id.cardSet with your actual ID
        cardBird = findViewById(R.id.cardBird) // Replace R.id.cardBird with your actual ID
        cardAbout = findViewById(R.id.cardAbout) // Replace R.id.cardAbout with your actual ID
        cardLogOut = findViewById(R.id.cardLogOut)

        cardMap.setOnClickListener {
            // Add your logic for cardMap onClick here
            showToast("CardMap Clicked")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        cardSet.setOnClickListener {
            // Add your logic for cardSet onClick here
            showToast("CardSet Clicked")
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        cardBird.setOnClickListener {
            // Add your logic for cardBird onClick here
            showToast("CardBird Clicked")
            val intent = Intent(this, Bird::class.java)
            startActivity(intent)
        }

        cardAbout.setOnClickListener {
            // Add your logic for cardAbout onClick here
            showToast("CardAbout Clicked")
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }

        cardLogOut.setOnClickListener {
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



