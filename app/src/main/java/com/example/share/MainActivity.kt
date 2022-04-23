package com.example.share

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.share.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var file: File
    private lateinit var selectedFileName: String
    private lateinit var selectedFileExtension: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chooseFileButton.setOnClickListener {
            chooseFile()
        }

        binding.shareButton.setOnClickListener {
            val newName = "${binding.newNameField.text}.$selectedFileExtension"
            shareFile(uriFromFile(file, newName))
        }
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        chooseFileResult.launch(intent)
    }

    private val chooseFileResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val intent = it.data
                val sourceUri = intent?.data
                selectedFileName = File(sourceUri?.path!!).name
                binding.selectedFileNameText.text = selectedFileName

                selectedFileExtension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(contentResolver.getType(sourceUri)).toString()
                val fileName = "temp.$selectedFileExtension"
                file = File(filesDir, fileName)

                copyFile(sourceUri, file)
            }
        }

    @SuppressLint("NewApi")
    private fun copyFile(sourceUri: Uri, targetFile: File) {
        contentResolver.openInputStream(sourceUri).use {inputStream ->
            FileOutputStream(targetFile).use {outputStream ->
                FileUtils.copy(inputStream!!, outputStream)
            }
        }
    }

    private fun shareFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share via..."))
    }

    private fun uriFromFile(file: File, displayName: String): Uri {
        return FileProvider.getUriForFile(
            this, "${BuildConfig.APPLICATION_ID}.provider", file, displayName)
    }
}