package com.me.o_chat.activities

import android.Manifest
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.me.o_chat.BuildConfig
import com.me.o_chat.R
import kotlinx.android.synthetic.main.activity_image_textureview.*
import kotlinx.android.synthetic.main.activity_station_create_p1.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit



// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.

private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)



class ImageCaptureActivity : AppCompatActivity() {

    private var btn: Button? = null
    private var imageview: ImageView? = null
    private val GALLERY = 1
    private val CAMERA = 2
    var selectedPhoto: Uri? = null
    var seletedPhotoDir: String = ""
    var storeLoc: String = ""
    lateinit var st: FirebaseStorage
    lateinit var db: FirebaseDatabase
    private var selectedPhotoPath: Uri? = null
    var eventName: String = ""
    var eventId: String = ""
    var teamId: String = ""
    var admin: String = ""
    var stationId: String = ""

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        admin = FirebaseAuth.getInstance().currentUser?.uid.toString()
        //   setContentView(R.layout.activity_image_capture)
        setContentView(R.layout.activity_image_textureview)
        //   btn = findViewById<View>(R.id.btn) as Button
        imageview = findViewById<View>(R.id.imageView) as ImageView
        //   btn!!.setOnClickListener { showPictureDialog() }

        viewFinder = findViewById(R.id.view_finder)

        st = FirebaseStorage.getInstance()

      //  if (intent.hasExtra("sUid")) {
      //      eventName = intent.getStringExtra("Event")
      //      stationId = intent.extras?.getString("sUid")!!
      //  }

        if (intent.hasExtra("sUid")) {
            stationId = intent.extras?.getString("sUid")!!
            eventId = intent.extras?.getString("EventId")!!
            eventName = intent.extras?.getString("EventN")!!
        }




        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        findViewById<Button>(R.id.btn_home).setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            // intent.putExtra("Kstation",station )
            startActivity(intent)

        }


    }


    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView


    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun startCamera() {

        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
        }.build()


        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }


        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)
        // findViewById<ImageButton>(R.id.capture_button).setOnClickListener {




        // camera button
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {

            //  val imagePath = File(filesDir, "images")
            //  val newFile = File(imagePath, "default_image.jpg")
            val newFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

            // selectedPhotoPath = getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", newFile)
            selectedPhotoPath = getUriForFile(this, "com.me.o_chat.fileprovider", newFile)
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

            Toast.makeText(baseContext, externalMediaDirs.first().toString(), Toast.LENGTH_SHORT)
                .show()
            Log.d("external", externalMediaDirs.first().toString())

            seletedPhotoDir = externalMediaDirs.first().toString()

            //  selectedPhotoPath = getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)
            //   selectedPhotoPath = file.toUri()
            // 3


            imageCapture.takePicture(file, executor, object : ImageCapture.OnImageSavedListener {
                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    exc: Throwable?
                ) {
                    val msg = "Photo capture failed: $message"
                    Log.e("CameraXApp", msg, exc)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                    }
                }

                override fun onImageSaved(file: File) {
                    val fileone = Uri.fromFile(file)
                    val stRef = st.reference.child("image/${fileone.lastPathSegment}")
                    var uploadTask = stRef.putFile(fileone)
                    uploadTask.addOnFailureListener {

                    }.addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                          //  FirebaseDatabase.getInstance().getReference().child(admin).child(eventId)
                            FirebaseDatabase.getInstance().getReference().child("events").child(eventId)
                                .child("stations").child(stationId)
                                .child("simage")
                                .setValue(taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                            Log.d(
                                "Saved image to db",
                                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                            )
//                           // FirebaseDatabase.getInstance().getReference().child(teamId).child(Event).child("images")
//                           .setValue(taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                        }


                    }

                    val urlTask = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        stRef.downloadUrl

                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                        } else {

                        }

                    }


                    val msg = "Photo capture succeeded: ${file.absolutePath}"
                    // storage/emulated/0/
                    // Android/media/com.me.o_chat/1583704551345.jpg
                    val msg2 = "Photo capture succeeded: ${file.toURI()}"
                    // file:/storage/emulated/0/Android/media/com.me.o_chat/1583704551345.jpg
                    val msg3 = "Photo capture succeeded: ${file.path}"
                    ///storage/emulated/0/Android/media/com.me.o_chat/1583704551345.jpg

                    val msg4 = "Photo capture succeeded: ${file.absolutePath}"

                    val msg5 =
                        " Photp external content URI {android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI}"

                    val msg6 =
                        " Photp internal content URI {android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI}"

                    val msg7 = "Photo capture succeeded url: ${file.toURL()}"
                    Log.d("CameraXApp", msg)
                    Log.d("CameraXApp", msg2)
                    Log.d("CameraXApp", msg3)
                    Log.d("CameraXApp", msg4)
                    Log.d("CameraXApp", msg5)
                    Log.d("CameraXApp", msg6)
                    Log.d("CameraXApp url", msg7)
                    viewFinder.post {
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            })


        }

        // end of Image Capture Button



        findViewById<Button>(R.id.select_btn).setOnClickListener {

            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            selectedPhotoPath =
                getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)

            choosePhotoFromGallary()

        }


        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, LuminosityAnalyzer())
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(
            this, preview, imageCapture, analyzerUseCase
        )


        //  CameraX.bindToLifecycle(this, preview, imageCapture)
        // CameraX.bindToLifecycle(this, preview)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }


    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimestamp = 0L

        /**
         * Helper extension function used to extract a byte array from an
         * image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
            val currentTimestamp = System.currentTimeMillis()
            // Calculate the average luma no more often than every second
            if (currentTimestamp - lastAnalyzedTimestamp >=
                TimeUnit.SECONDS.toMillis(1)
            ) {
                // Since format in ImageAnalysis is YUV, image.planes[0]
                // contains the Y (luminance) plane
                val buffer = image.planes[0].buffer
                // Extract image data from callback object
                val data = buffer.toByteArray()
                // Convert the data into an array of pixel values
                val pixels = data.map { it.toInt() and 0xFF }
                // Compute average luminance for the image
                val luma = pixels.average()
                // Log the new luma value
                Log.d("CameraXApp", "Average luminosity: $luma")
                // Update timestamp of last analyzed frame
                lastAnalyzedTimestamp = currentTimestamp
            }
        }
    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallary()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }


    fun choosePhotoFromGallary() {

        Log.d("external G", Environment.getExternalStorageDirectory().toString())
        Log.d("external Gall Int", seletedPhotoDir.toUri().toString())
        Log.d("external Gall Int", MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        Log.d("external PUT EXTRA", selectedPhotoPath.toString())
        galleryIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedPhotoPath)

        //         MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // /storage/emulated/0/Android/media/o_chat
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI)


        startActivityForResult(galleryIntent, GALLERY)
    }


    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/
        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val contentURI = data!!.data
                Log.d("onAct Result", contentURI.toString())
                try {
                       val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                       val path = saveImage(bitmap)
                       Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
                        selectedPhoto = contentURI
                       imageview!!.setImageBitmap(bitmap)

                    setImageViewWithImage()


                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            imageview!!.setImageBitmap(thumbnail)

            saveImage(thumbnail)
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdirs()
        }

        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance()
                    .getTimeInMillis()).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.getPath()),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            Log.d("TAG", "Wallpaper:) File Saved::--->" + f.getAbsolutePath())

            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }


    private fun letsStoreImageToStorage() {
        //runBlocking {
        storeLoc = ""
        val fileName = UUID.randomUUID().toString()
        var store = st.getReference("/Image/${fileName}")

        store.putFile(selectedPhoto!!).addOnSuccessListener {
            Log.d("msg", "Uploaded Image to storage ${it.metadata?.path}")
            store.downloadUrl.addOnSuccessListener {
                Log.d("msg", "saving storage ref to DB ${it.toString()}")
                // saveImageSorageRefToDataBase(it.toString())
                storeLoc = it.toString()

            }
        }
        //  }
        //  Log.d("msg", "This is the ref that is being returned ${storeLoc}")
        // return storeLoc
    }


    private fun setImageViewWithImage() {
        val photoPath: Uri = selectedPhotoPath ?: return
        imageView.post {
            val pictureBitmap = BitmapResizer.shrinkBitmap(
                this@ImageCaptureActivity,
                photoPath,
                imageView.width,
                imageView.height
            )
            imageView.setImageBitmap(pictureBitmap)
        }
        //  lookingGoodTextView.visibility = View.VISIBLE
        //  pictureTaken = true
    }



    companion object {
       //
        private val IMAGE_DIRECTORY = "/demoPics"

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }


}