package com.codemotion.pro

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.content.ContentValues
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputType
import java.io.File
import java.io.FileInputStream

class MainActivity : Activity() {
    private lateinit var editor: EditText
    private lateinit var preview: TextView
    private lateinit var status: TextView
    private lateinit var exportButton: Button
    private var language = "Kotlin"
    private var resolution = "720p"
    private var isIndonesian = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(22, 27, 36)
        window.navigationBarColor = Color.rgb(22, 27, 36)
        buildInterface()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(18, 22, 29))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "CodeMotion Pro"
            textSize = 25f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        val localeButton = Button(this).apply { text = if (isIndonesian) "ID" else "EN" }
        localeButton.setOnClickListener {
            isIndonesian = !isIndonesian
            buildInterface()
        }
        header.addView(localeButton)
        root.addView(header)
        root.addView(TextView(this).apply {
            text = if (isIndonesian) "EDITOR KODE  •  STUDIO VIDEO OFFLINE" else "CODE EDITOR  •  OFFLINE VIDEO STUDIO"
            textSize = 11f; setTextColor(Color.rgb(135, 160, 190))
            setPadding(0, 4, 0, 12)
        })
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val langs = Spinner(this)
        langs.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Kotlin","Java","Python","JavaScript","HTML","C++","JSON"))
        langs.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { language = langs.selectedItem.toString() }
        }
        val sizes = Spinner(this)
        sizes.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("720p","1080p"))
        sizes.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { resolution = sizes.selectedItem.toString() }
        }
        controls.addView(langs, LinearLayout.LayoutParams(0, 52, 1f))
        controls.addView(sizes, LinearLayout.LayoutParams(0, 52, 1f))
        root.addView(controls)
        editor = EditText(this).apply {
            setText("fun main() {\n    println(\"Hello, CodeMotion!\")\n}")
            setTextColor(Color.rgb(220, 230, 240))
            setHintTextColor(Color.GRAY)
            hint = if (isIndonesian) "Tempel kode program di sini…" else "Paste source code here…"
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setBackgroundColor(Color.rgb(29, 36, 47))
            setPadding(14, 14, 14, 14)
            minLines = 9
        }
        root.addView(editor, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply { setMargins(0, 12, 0, 12) })
        root.addView(TextView(this).apply {
            text = if (isIndonesian) "PRATINJAU KODE" else "CODE PREVIEW"
            setTextColor(Color.rgb(100, 180, 255)); textSize = 11f
        })
        preview = TextView(this).apply {
            text = if (isIndonesian) "Pratinjau kode akan tampil di sini." else "Your code preview appears here."
            setTextColor(Color.LTGRAY); typeface = android.graphics.Typeface.MONOSPACE; textSize = 12f
            setBackgroundColor(Color.rgb(29, 36, 47)); setPadding(12, 12, 12, 12)
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, 100).apply { setMargins(0, 6, 0, 10) })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val previewButton = Button(this).apply { text = if (isIndonesian) "Pratinjau" else "Preview" }
        previewButton.setOnClickListener { preview.text = editor.text.toString().take(600) }
        exportButton = Button(this).apply { text = if (isIndonesian) "Ekspor MP4" else "Export MP4" }
        exportButton.setOnClickListener { exportVideo() }
        actions.addView(previewButton, LinearLayout.LayoutParams(0, 54, 1f))
        actions.addView(exportButton, LinearLayout.LayoutParams(0, 54, 1f))
        root.addView(actions)
        status = TextView(this).apply {
            text = if (isIndonesian) "Siap • Masukkan kode lalu ekspor video." else "Ready • Enter code, then export video."
            setTextColor(Color.rgb(160, 180, 200)); textSize = 12f; setPadding(0, 8, 0, 0)
        }
        root.addView(status)
        setContentView(root)
    }

    private fun exportVideo() {
        val source = editor.text.toString()
        if (source.isBlank()) {
            status.text = if (isIndonesian) "Masukkan kode terlebih dahulu." else "Enter some code first."
            return
        }
        exportButton.isEnabled = false
        status.text = if (isIndonesian) "Sedang merender video…" else "Rendering video…"
        Thread {
            try {
                val tempFile = CodeVideoRenderer(this).render(source, language, resolution)
                val savedUri = saveToGallery(tempFile)
                runOnUiThread {
                    exportButton.isEnabled = true
                    status.text = if (isIndonesian) "Video tersimpan di Movies/CodeMotion Pro" else "Video saved to Movies/CodeMotion Pro"
                    Toast.makeText(this, if (isIndonesian) "Ekspor video berhasil" else "Video export complete", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    exportButton.isEnabled = true
                    status.text = (if (isIndonesian) "Ekspor gagal: " else "Export failed: ") + (e.message ?: "unknown error")
                }
            }
        }.start()
    }

    private fun saveToGallery(source: File): Uri? {
        val name = source.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CodeMotion Pro")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Tidak dapat membuat file video di galeri")
            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(source).use { input -> input.copyTo(output) }
                } ?: throw IllegalStateException("Tidak dapat menulis file video")
                val complete = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
                contentResolver.update(uri, complete, null, null)
            } catch (e: Exception) {
                contentResolver.delete(uri, null, null)
                throw e
            }
            source.delete()
            return uri
        }
        val directory = File(getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES), "CodeMotion Pro")
        if (!directory.exists() && !directory.mkdirs()) throw IllegalStateException("Tidak dapat membuat folder video")
        val destination = File(directory, name)
        source.copyTo(destination, overwrite = true)
        source.delete()
        MediaScannerConnection.scanFile(this, arrayOf(destination.absolutePath), arrayOf("video/mp4"), null)
        return null
    }
}