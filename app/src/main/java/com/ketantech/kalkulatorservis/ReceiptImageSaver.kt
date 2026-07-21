package com.ketantech.kalkulatorservis

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import java.io.OutputStream

/**
 * Helper untuk render view menjadi Bitmap dan menyimpannya ke galeri (Foto Nota).
 */
object ReceiptImageSaver {

    /** Render view ke Bitmap dengan background putih. */
    fun captureView(view: View): Bitmap {
        val width = view.width
        val height = view.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    /** Simpan bitmap ke galeri Pictures/Ketantech/. Mengembalikan URI atau null jika gagal. */
    fun saveToGallery(context: Context, bitmap: Bitmap, filename: String): Uri? {
        val displayName = if (filename.endsWith(".png")) filename else "$filename.png"
        val relativePath = Environment.DIRECTORY_PICTURES + "/Ketantech"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        var uri: Uri? = null
        var stream: OutputStream? = null
        try {
            uri = resolver.insert(collection, values) ?: return null
            stream = resolver.openOutputStream(uri) ?: return null
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            // Fallback: hapus entry yang gagal
            uri?.let { resolver.delete(it, null, null) }
            return null
        } finally {
            stream?.close()
        }
    }
}
