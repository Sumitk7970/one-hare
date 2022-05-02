package com.example.share

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfGState
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.File
import java.io.FileOutputStream

/** load the pdf into pdfview */
fun Uri.loadIntoPDFView(pdfView: PDFView) {
    pdfView.fromUri(this)
        .enableSwipe(true)
        .load()
}

/** load the pdf into pdfview */
fun File.loadIntoPDFView(pdfView: PDFView) {
    pdfView.fromFile(this)
        .enableSwipe(true)
        .load()
}

/** Returns the file name from its uri */
fun Uri.fileName(context: Context): String? {
    var result: String? = null
    context.contentResolver.query(this, null, null, null, null)
        .use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                Log.d(TAG, "fileName: $result")
            }
        }
    return result
}

/** Copies the content of the source file to the target file */
fun Uri.copyTo(context: Context, targetFile: File) {
    context.contentResolver.openInputStream(this).use { inputStream ->
        FileOutputStream(targetFile).use { outputStream ->
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream!!.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }
        }
    }
}

/**
 * Adds text watermark to a pdf file
 * @param watermarkText text to be added as a watermark
 * @param fileWaterMark to store the content of the file after watermarking
 * @param opacity the opacity of the watermark text
 */
fun File.addWatermark(watermarkText: String, fileWaterMark: File, opacity: Float = .25f) {
    try {
        val pdfReader = PdfReader(this.path)
        val pdfStamper = PdfStamper(pdfReader, FileOutputStream(fileWaterMark))

        for (i in 1..pdfReader.numberOfPages) {
            val pdfContentByte = pdfStamper.getOverContent(i)
            val rectanglePageSize = pdfReader.getPageSize(i)
            val horizontalMidPosition =
                (rectanglePageSize.left + rectanglePageSize.right) / 2
            val verticalMidPosition =
                (rectanglePageSize.top + rectanglePageSize.bottom) / 2

            val pdfGState = PdfGState()
            pdfGState.setFillOpacity(opacity)
            pdfContentByte.setGState(pdfGState)

            ColumnText.showTextAligned(
                pdfContentByte, Element.ALIGN_CENTER,
                Phrase(watermarkText, Font(Font.FontFamily.TIMES_ROMAN, 45f)),
                horizontalMidPosition, verticalMidPosition, 45f
            )
        }
        pdfStamper.close()
        pdfReader.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Returns the content uri of the file with its name changed by the provided displayName
 * @return content uri of the file
 */
fun File.uri(context: Context, displayName: String): Uri {
    return FileProvider.getUriForFile(
        context, "${BuildConfig.APPLICATION_ID}.provider", this, displayName
    )
}