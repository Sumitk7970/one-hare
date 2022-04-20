package com.example.share

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.share.adapters.FilesAdapter
import com.example.share.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

//        val projection = arrayOf(MediaStore.DownloadColumns._ID)
//        val selection = null
//        val selectionArgs = null
//        val sortOrder = null
//
//        applicationContext.contentResolver.query(
//            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
//            projection,
//            selection,
//            selectionArgs,
//            sortOrder
//        )?.use { cursor ->
//            while (cursor.moveToNext()) {
//                // Use an ID column from the projection to get
//                // a URI representing the media item itself.
//
//            }
//        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = FilesAdapter(arrayListOf("Virat", "Rohit", "Rahul"))
    }

    private fun getPdfList(): ArrayList<String> {
        val pdfList: ArrayList<String> = ArrayList()
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, null, null, null)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
//                        pdfList.add(cursor.getString(columnData))
                        pdfList.add("cursor.getString(columnData)")
                        Log.d(TAG, "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        Log.d(TAG, "getPdfList: ${pdfList.size}")
        return pdfList
    }
}