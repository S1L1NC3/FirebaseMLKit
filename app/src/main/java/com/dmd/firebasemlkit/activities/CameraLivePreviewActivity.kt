package com.dmd.firebasemlkit.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dmd.firebasemlkit.R
import com.dmd.firebasemlkit.databinding.ActivityCameraPreviewBinding
import com.dmd.firebasemlkit.enums.LiveProcessTypes
import com.dmd.helpers.ToastHelper
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraLivePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPreviewBinding
    private var cameraSelector: CameraSelector? = null
    private var previewView: PreviewView? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null


    private val screenAspectRatio: Int
        get() {
            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { previewView?.display?.getRealMetrics(it) }
            return aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }

    companion object {
        private val TAG = CameraLivePreviewActivity::class.java.simpleName
        private const val PERMISSION_CAMERA_REQUEST = 1

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }


    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }


    fun changeCameraAction(view: View){
        startCamera()
        ToastHelper.shortShow(applicationContext, resources.getString(R.string.changed_camera))
    }

    override fun onStart() {
        super.onStart()

        val result: LiveProcessTypes = intent.getSerializableExtra(resources.getString(R.string.intent_proces_type)) as LiveProcessTypes
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        binding.viewFinder.bitmap



        when (result){
            LiveProcessTypes.BarcodeScan -> scanBarcode()
            LiveProcessTypes.FaceDetection -> print("denem")
            LiveProcessTypes.ObjectDetectionAndTrack -> print("denem")
            else -> {
                print("dene")
            }
        }
        //if (result == LiveProcessTypes.BarcodeScan)
        ToastHelper.shortShow(applicationContext, result.name)
        startCamera()

        //TODO At every open camera and other permission check
    }

    private fun scanBarcode(){
        /*val scanner = BarcodeScanning.getClient()
        scanner.process()
            .addOnSuccessListener { barcodes ->
                // Task completed successfully
                ToastHelper.shortShow(applicationContext, barcodes.size.toString())
            }
            .addOnFailureListener {
                // Task failed with an exception
                ToastHelper.shortShow(applicationContext, it.toString())
            }
            .addOnCompleteListener{

            }*/
    }

    private fun method() : LiveData<ProcessCameraProvider>{
        if (cameraProviderLiveData == null) {
            cameraProviderLiveData = MutableLiveData()
            val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(getApplication())
            cameraProviderFuture.addListener(
                    Runnable {
                        try {
                            cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                        } catch (e: ExecutionException) {
                            // Handle any errors (including cancellation) here.
                            Log.e(TAG, "Unhandled exception", e)
                        } catch (e: InterruptedException) {
                            Log.e(TAG, "Unhandled exception", e)
                        }
                    },
                    ContextCompat.getMainExecutor(getApplication())
            )
        }
        return cameraProviderLiveData!!
    }

    private fun startCamera() {
        previewView = binding.viewFinder
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        method().observe(
                this,
                Observer { provider: ProcessCameraProvider? ->
                    cameraProvider = provider
                    bindPreviewUseCase()
                    bindAnalyseUseCase()
                }
        )
        /*
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

            try {
                cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA){
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                        this, cameraSelector!!, preview
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))*/

        //bindAnalyseUseCase()
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(previewView!!.display.rotation)
                .build()
        previewUseCase!!.setSurfaceProvider(previewView!!.surfaceProvider)

        try {
            cameraProvider!!.bindToLifecycle(
                    /* lifecycleOwner= */this,
                    cameraSelector!!,
                    previewUseCase
            )
        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.message?.let { Log.e(TAG, it) }
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.message?.let { Log.e(TAG, it) }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun processImageProxy(
            barcodeScanner: BarcodeScanner,
            imageProxy: ImageProxy
    ) {
        val inputImage =
                InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.forEach {
                        Log.d(TAG, it.boundingBox.toString())
                    }
                }
                .addOnFailureListener {
                    it.message?.let { it1 -> Log.e(TAG, it1) }
                }.addOnCompleteListener {
                    // When the image is from CameraX analysis use case, must call image.close() on received
                    // images when finished using them. Otherwise, new images may not be received or the camera
                    // may stall.
                    imageProxy.close()
                }
    }

    private fun bindAnalyseUseCase() {
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        analysisUseCase = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(previewView!!.display.rotation)
                .build()

        // Initialize our background executor
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analysisUseCase?.setAnalyzer(
                cameraExecutor,
                { imageProxy ->
                    processImageProxy(barcodeScanner, imageProxy)
                }
        )

        try {
            cameraProvider!!.bindToLifecycle(
                    /* lifecycleOwner= */this,
                    cameraSelector!!,
                    analysisUseCase
            )
        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.message?.let { Log.e(TAG, it) }
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException?.message?.let { Log.e(TAG, it) }
        }
    }



}



