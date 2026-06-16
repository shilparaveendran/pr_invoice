package com.app.printf.util

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

object PdfRenderUtils {
    fun renderPages(file: File): List<ImageBitmap> {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val pages = mutableListOf<ImageBitmap>()
        try {
            for (index in 0 until renderer.pageCount) {
                val page = renderer.openPage(index)
                try {
                    val scale = 2
                    val bitmap = Bitmap.createBitmap(
                        page.width * scale,
                        page.height * scale,
                        Bitmap.Config.ARGB_8888,
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pages.add(bitmap.asImageBitmap())
                } finally {
                    page.close()
                }
            }
        } finally {
            renderer.close()
            descriptor.close()
        }
        return pages
    }
}
