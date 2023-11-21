package com.gigglegang.openedmaps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class Camera : AppCompatActivity() {

    //variables
    private var imgViewCamera: ImageView? = null
    private var btnTakePic: Button? = null
    private var storageRef: StorageReference? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().reference

        //Typecasting
        imgViewCamera = findViewById(R.id.cameraImg)
        btnTakePic = findViewById(R.id.takePic)

        // Set click listener for the button
        btnTakePic?.setOnClickListener {
            captureOnClick()
        }
    }

    private fun captureOnClick() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 0)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            super.onActivityResult(requestCode, resultCode, data)
            val bm = data?.extras?.get("data") as Bitmap?
            imgViewCamera?.setImageBitmap(bm)

            // Convert bitmap to byte array

            val baos = ByteArrayOutputStream()
            bm?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData: ByteArray = baos.toByteArray()

            // Generate a unique filename (e.g., using a timestamp)
            val filename = "${System.currentTimeMillis()}.jpg"

            // Upload the image to Firebase Storage
            val imageRef = storageRef?.child(filename)
            val uploadTask = imageRef?.putBytes(imageData)
            uploadTask?.addOnSuccessListener {

                // Image upload successful
                Toast.makeText(this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT)
                    .show()
            }?.addOnFailureListener {

                // Image upload failed
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }

        } catch (ex: Exception) {
            Toast.makeText(this, "Pic not saved", Toast.LENGTH_SHORT).show()
        }
    }

}
