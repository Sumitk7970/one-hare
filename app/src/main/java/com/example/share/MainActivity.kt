package com.example.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.share.databinding.ActivityMainBinding
import java.io.File

const val TEMP_FILE_NAME = "temp.pdf"
const val TEMP_FILE_WATERMARK_NAME = "temp_watermark.pdf"

class MainActivity : AppCompatActivity() {
    private lateinit var fileOperations: FileOperations
    private lateinit var selectedFileCopy: File
    private lateinit var watermarkFile: File
    private lateinit var currentFile: File
    private lateinit var selectedFileUri: Uri
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedFileCopy = File(filesDir, TEMP_FILE_NAME)
        watermarkFile = File(filesDir, TEMP_FILE_WATERMARK_NAME)
        currentFile = selectedFileCopy

        fileOperations = FileOperations()

        handleIntent()

        binding.chooseFileButton.setOnClickListener {
            chooseFile()
        }
        binding.confirmButton.setOnClickListener {
            val displayName = binding.newNameField.text.toString() + ".pdf"
            val fileUri = fileOperations.getUriFromFile(this, currentFile, displayName)
            if (intent.action == Intent.ACTION_GET_CONTENT) {
                returnFileToIntentRequest(fileUri)
            } else {
                shareFile(fileUri)
            }
        }

        binding.addWatermarkButton.setOnClickListener {
            val watermarkText = binding.waterMarkTextField.text.toString()
            fileOperations.addWaterMark(watermarkText, selectedFileCopy, watermarkFile)
            currentFile = watermarkFile
        }
    }

    /**
     * Changes the ui accordingly if the user has come from a Intent.ACTION_GET_CONTENT of another
     * app
     */
    private fun handleIntent() {
        binding.confirmButton.text = if (intent.action == Intent.ACTION_GET_CONTENT) {
            "Confirm"
        } else {
            "Share"
        }
    }

    /** Handles the result after user chooses a file */
    private val chooseFileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = it.data
            selectedFileUri = intent?.data!!

            // copying the selected file
            fileOperations.copyFile(this, selectedFileUri, selectedFileCopy)

            updateFileNameTextView()
        }
    }

    /** send the user to the file chooser of android to select a file */
    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        chooseFileResult.launch(intent)
    }

    /** Displays the name of the file in fileNameTextView */
    private fun updateFileNameTextView() {
        binding.selectedFileNameTextView.text = File(selectedFileUri.path!!).name
    }

    /**
     * Shares the file
     * @param fileUri uri of the file which is to be shared
     */
    private fun shareFile(fileUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = contentResolver.getType(fileUri)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            // giving read and write permissions of the file
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivity(intent)
    }

    /**
     * Return the file to the application which has requested by Intent.ACTION_GET_CONTENT
     * @param fileUri uri of the file which is to be sent
     */
    private fun returnFileToIntentRequest(fileUri: Uri) {
        val resultIntent = Intent("com.example.share.ACTION_RETURN_FILE").apply {
            setDataAndType(fileUri, contentResolver.getType(fileUri))
            // giving read and write permissions of the file
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}