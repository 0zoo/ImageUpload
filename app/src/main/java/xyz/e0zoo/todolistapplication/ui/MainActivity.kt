package xyz.e0zoo.todolistapplication.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.RequestBody
import xyz.e0zoo.todolistapplication.R
import xyz.e0zoo.todolistapplication.api.authApi
import xyz.e0zoo.todolistapplication.api.provideApi
import xyz.e0zoo.todolistapplication.utils.enqueue
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_GALLERY_IMAGE = 2
    }

    private var filename: String? = null
    private var mPhotoFile: File? = null
    private var mContentType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadButton.isEnabled = false

        uploadButton.setOnClickListener {
            showPictureDialog()
        }

        downloadButton.setOnClickListener {
            if (!filename.isNullOrBlank()) {
                GlideApp.with(this)
                        .load("http://10.0.2.2:3000/images/$filename")
                        .into(imageView)
            }
        }

    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("gallery", "camera")
        pictureDialog.setItems(pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {

        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            type = "image/*"
        }

        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }


    @Throws(IOException::class)
    private fun createTempFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }


    private fun takePhotoFromCamera() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {

                mPhotoFile = try {
                    createTempFile()
                } catch (ex: IOException) {
                    Log.i("MainActivity", ex.message)
                    null
                }

                mPhotoFile?.also { file ->
                    val photoUri = FileProvider.getUriForFile(
                            this,
                            "xyz.e0zoo.todolistapplication.fileprovider",
                            file
                    )
                    mContentType = contentResolver.getType(photoUri)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) return

        if (requestCode == REQUEST_TAKE_PHOTO) {

            mPhotoFile?.also { file ->
                mContentType?.also { contentType ->
                    uploadFile(file, contentType)
                    mPhotoFile = null
                    mContentType = null
                }
            }

        } else if (requestCode == REQUEST_GALLERY_IMAGE && data != null) {

            val fileUri = data.data ?: return
            val tempFile = createTempFile()
            val inputStream = contentResolver.openInputStream(fileUri) ?: return
            tempFile.outputStream().use { inputStream.copyTo(it) }

            val contentType = contentResolver.getType(fileUri)

            uploadFile(tempFile, contentType)
        }
    }

    private fun uploadFile(file: File, contentType: String) {

        val requestBody = RequestBody.create(MediaType.parse(contentType), file)

        provideApi(this).postImage().enqueue({ response ->
            response.body()?.let { Image ->

                authApi.putImage(Image.signedUrl, requestBody).enqueue({ _ ->
                    downloadButton.isEnabled = true
                    filename = Image.filename

                    file.delete()

                }, {
                    Log.i("MainActivity", it.message)
                })
            }
        }, {
            Log.i("MainActivity", it.message)
        })

    }


}

@GlideModule
class ChatAppGlideModule : AppGlideModule()
