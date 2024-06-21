package com.example.screenrecordingtestapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {


    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var imageReader: ImageReader
    private var isRecording = false
    private var outputFile: FileOutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnStartStop = findViewById<Button>(R.id.btnStartStop)

        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        btnStartStop.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun startRecording() {

        startService(Intent(this, MediaService::class.java))

        val intent = mediaProjectionManager.createScreenCaptureIntent()
//        startActivityForResult(intent, REQUEST_CODE_CAPTURE)

        startCaptureResultLauncher.launch(intent)
    }

    private fun stopRecording() {
        mediaProjection.stop()
        imageReader.close()
        outputFile?.close()
        isRecording = false
        Toast.makeText(this, "Screen recording stopped", Toast.LENGTH_SHORT).show()
    }


    private val startCaptureResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let { it ->
                startRecordingMedia(it)
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            startRecordingMedia(data!!)
//        }
//    }

    private fun startRecordingMedia(data: Intent) {

        val displayMetrics = this.resources.displayMetrics

//        val width = windowManager.currentWindowMetrics.bounds.width()
//        val height = windowManager.currentWindowMetrics.bounds.height()

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data)!!
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
//        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.YUV_422_888, 2)

        val virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenRecording",
            width,
            height,
            displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
            imageReader.surface,
            null,
            null
        )

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                saveYUVFrame(image)
                image.close()
            }
        }, Handler(Looper.getMainLooper()))

        isRecording = true
        Toast.makeText(this, "Screen recording started", Toast.LENGTH_SHORT).show()
    }

    private fun saveYUVFrame(image: Image) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val ySize = buffer.remaining()

        val yuvBytes = ByteArray(ySize)
        buffer.get(yuvBytes)

        println("save yuv image ${image.width} ${image.height}")

//        try {
//            if (outputFile == null) {
//                val filePath =
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//                        .toString() + "/ScreenRecordings/"
//                val fileDir = File(filePath)
//                if (!fileDir.exists()) {
//                    fileDir.mkdirs()
//                }
//                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
//                val date = Date()
//                val fileName = "$filePath/screen_recording_${dateFormat.format(date)}.yuv"
//                outputFile = FileOutputStream(fileName)
//            }
//            outputFile?.write(yuvBytes)
//        } catch (e: IOException) {
//            Log.e("MainActivity", "Failed to save YUV frame: ${e.message}")
//        }
    }

}