package com.example.share

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.share.databinding.ActivityMainBinding
import java.io.File

const val TEMP_FILE_NAME = "temp.pdf"
const val TEMP_FILE_WATERMARK_NAME = "temp_watermark.pdf"

class MainActivity : AppCompatActivity() {
    private lateinit var selectedFileCopy: File
    private lateinit var watermarkFile: File
    private lateinit var currentFile: File
    private lateinit var selectedFileUri: Uri
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // redirecting the user to select a file
        Toast.makeText(this, "Select a file", Toast.LENGTH_SHORT).show()
        chooseFile()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedFileCopy = File(filesDir, TEMP_FILE_NAME)
        watermarkFile = File(filesDir, TEMP_FILE_WATERMARK_NAME)
        currentFile = selectedFileCopy

        handleIntent()

        binding.btnConfirm.setOnClickListener {
            val displayName = binding.etNewName.text.toString() + ".pdf"
            val fileUri = currentFile.uri(this, displayName)
            if (intent.action == Intent.ACTION_GET_CONTENT) {
                returnFileToIntentRequest(fileUri)
            } else {
                shareFile(fileUri)
            }
        }

        binding.btnAddWatermark.setOnClickListener {
            showDialog()
        }

        binding.btnChooseFile.setOnClickListener {
            chooseFile()
        }

        binding.btnCancel.setOnClickListener {
            cancel()
        }
    }

    private fun showDialog() {
        // creating a edittext and focusing on it
        val etWatermarkText = EditText(this)
        etWatermarkText.requestFocus()

        val alertDialog = AlertDialog.Builder(this).apply {
            title = "Add watermark"
            setMessage("Enter watermark text")
            setView(etWatermarkText)
            setPositiveButton("Add") { _, _ ->
                val watermarkText = etWatermarkText.text.toString().trim()

                // watermarking the file and loading it int the PDFView
                if (watermarkText.isNotBlank()) {
                    selectedFileCopy.addWatermark(watermarkText, watermarkFile)
                    currentFile = watermarkFile
                    watermarkFile.loadIntoPDFView(binding.pdfView)
                }
            }
            setNegativeButton(getString(R.string.cancel), null)
            create()
        }
        alertDialog.show()
    }

    /**
     * Changes the ui accordingly if the user has come from a Intent.ACTION_GET_CONTENT of another
     * app
     */
    private fun handleIntent() {
        if (intent.action == Intent.ACTION_GET_CONTENT) {
            binding.btnConfirm.text = getString(R.string.confirm)
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
            selectedFileUri.copyTo(this, selectedFileCopy)

            updateFileNameTextView()
            selectedFileUri.loadIntoPDFView(binding.pdfView)
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
        binding.tvFileName.text = selectedFileUri.fileName(this)
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

    private fun cancel() {
        finish()
    }
}