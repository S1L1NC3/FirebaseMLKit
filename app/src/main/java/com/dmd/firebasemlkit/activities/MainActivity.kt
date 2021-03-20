package com.dmd.firebasemlkit.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.dmd.firebasemlkit.R
import com.dmd.firebasemlkit.databinding.ActivityMainBinding
import com.dmd.firebasemlkit.enums.LiveProcessTypes
import com.dmd.helpers.ToastHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_IMAGE_CAPTURE_FOR_TEXT_RECOGNIZE = 1
    private lateinit var photoPath: String
    private lateinit var photoUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.textRecognizeScan.setOnClickListener {
            openCamera(REQUEST_IMAGE_CAPTURE_FOR_TEXT_RECOGNIZE)
        }

        binding.barcodeScan.setOnClickListener {
            openIntent(LiveProcessTypes.BarcodeScan)
            //openCamera(REQUEST_IMAGE_CAPTURE_SCAN_BARCODE)
        }

        binding.faceDetectionScan.setOnClickListener {
            openIntent(LiveProcessTypes.FaceDetection)
        }

        binding.copyToClipboard.setOnClickListener {
            copyToClipboard(binding.resultText.text.toString())
        }

        binding.objectDetectionScan.setOnClickListener {
            openIntent(LiveProcessTypes.ObjectDetectionAndTrack)
        }

    }

    private fun openIntent(type: LiveProcessTypes){
        val intent = Intent(this@MainActivity, CameraLivePreviewActivity::class.java)
        intent.putExtra(resources.getString(R.string.intent_proces_type), type)
        startActivity(intent)
    }

    private fun copyToClipboard(textToCopy: String){
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        ToastHelper.shortShow(applicationContext, resources.getString(R.string.copied))
    }

    private fun openCamera(requestCode: Int){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null){
            var photoFile: File? = null
            try{
                photoFile = createFromFile()
            } catch (e: IOException){}

            if (photoFile != null){
                photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.android.fileprovider",
                    photoFile
                )

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, requestCode)
            }
        }
    }

    private fun textRecognition(inputImage: InputImage){
        val recognizer = TextRecognition.getClient()
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                binding.resultText.text = visionText.text
            }
            .addOnFailureListener {
                // Task failed with an exception
                binding.resultText.text = it.printStackTrace().toString()
            }
    }

    private fun createFromFile() : File?{
        val fileName = "MyPic"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            fileName,
            ".jpg",
            storageDir
        )
        photoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK){
            val inputImage: InputImage = InputImage.fromFilePath(applicationContext, photoUri)
            binding.imageView.setImageURI(photoUri)
            when (requestCode){
                REQUEST_IMAGE_CAPTURE_FOR_TEXT_RECOGNIZE -> textRecognition(inputImage)
                else -> {

                }
            }
        }


    }

}


