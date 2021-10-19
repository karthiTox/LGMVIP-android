package com.proj.facedetection

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.annotation.RequiresApi
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView;
    private var imageUri = Uri.EMPTY;

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById<ImageView>(R.id.image_view)

        findViewById<Button>(R.id.select_img_btn).setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startForResult.launch(Intent.createChooser(i, "Choose Image"))
        }

        findViewById<Button>(R.id.process_img_btn).setOnClickListener {
            detectFace()
        }

    }

    private val startForResult = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            imageView.setImageURI(result.data?.data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun detectFace() {
        if(imageUri == Uri.EMPTY) return

        Toast.makeText(this, "processing the image!", Toast.LENGTH_LONG).show()

        val image = InputImage.fromFilePath(this, imageUri)

        var tempBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
        tempBitmap = tempBitmap.copy(Bitmap.Config.ARGB_8888, true);
        val tempCanvas = Canvas(tempBitmap)

        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(highAccuracyOpts)

        detector.process(image).addOnSuccessListener {
            for (face in it) {
                val bounds = face.boundingBox

                val p = Paint()
                p.color = Color.rgb(98, 0, 238)
                p.alpha = 80
                tempCanvas.drawRect(bounds, p)

                imageView.setImageDrawable(BitmapDrawable(resources, tempBitmap))
                Toast.makeText(this, "successfully processed the image!", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Log.wtf("MainActivity", it.message.toString())
        }
    }
}