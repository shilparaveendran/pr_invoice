package com.app.printf.util



import android.content.Context

import android.graphics.Bitmap

import android.graphics.BitmapFactory

import android.graphics.ImageDecoder

import android.net.Uri

import android.os.Build

import java.io.File

import java.io.FileOutputStream

import kotlin.math.min



object SignatureStorage {

    private const val SIGNATURE_FILE = "profile_signature.png"



    fun signatureFile(context: Context): File {

        val dir = File(context.filesDir, "signatures").apply { mkdirs() }

        return File(dir, SIGNATURE_FILE)

    }



    fun save(context: Context, bitmap: Bitmap): String {

        val file = signatureFile(context)

        FileOutputStream(file).use { output ->

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)

        }

        return file.absolutePath

    }



    fun load(path: String): Bitmap? {

        if (path.isBlank()) return null

        val file = File(path)

        if (!file.exists()) return null

        return BitmapFactory.decodeFile(file.absolutePath)

    }



    fun delete(path: String) {

        if (path.isBlank()) return

        File(path).delete()

    }



    fun loadFromUri(context: Context, uri: Uri): Bitmap? {

        return try {

            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                val source = ImageDecoder.createSource(context.contentResolver, uri)

                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->

                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

                }

            } else {

                context.contentResolver.openInputStream(uri)?.use { input ->

                    BitmapFactory.decodeStream(input)

                }

            }

            decoded?.let { scaleForSignature(it) }

        } catch (_: Exception) {

            null

        }

    }



    fun scaleForSignature(bitmap: Bitmap): Bitmap {

        val maxDim = 800

        if (bitmap.width <= maxDim && bitmap.height <= maxDim) return bitmap

        val scale = min(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)

        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)

        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, width, height, true)

    }

}


