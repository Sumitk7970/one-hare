package com.example.share

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import androidx.core.content.FileProvider
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.*
import java.io.File
import java.io.FileOutputStream

class FileOperations {

    /** Copies the content of the source file to the target file */
    fun copyFile(context: Context, sourceUri: Uri, targetFile: File) {
        context.contentResolver.openInputStream(sourceUri).use {inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.copy(inputStream!!, outputStream)
                } else {
                    TODO("VERSION.SDK_INT < Q")
                }
            }
        }
    }

    /**
     * Adds text watermark to a pdf file
     * @param file the source file
     * @param fileWaterMark to store the content of the file after watermarking
     * @param opacity the opacity of the watermark text
     */
    fun addWaterMark(watermarkText: String, file: File, fileWaterMark: File, opacity: Float = .25f) {
        try {
            val pdfReader = PdfReader(file.path)
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
    fun getUriFromFile(context: Context, file: File, displayName: String): Uri {
        return FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.provider", file, displayName
        )
    }
}