package com.codemotion.pro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private lateinit var editor: EditText
    private lateinit var preview: TextView
    private lateinit var status: TextView
    private var language = "Kotlin"
    private var resolution = "720p"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(22, 27, 36)
        window.navigationBarColor = Color.rgb(22, 27, 36)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(Color.rgb(18, 22, 29))
        }
        root.addView(TextView(this).apply {
            text = "CodeMotion Pro"
            textSize = 25f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "CODE → VIDEO  •  Offline studio"
            textSize = 12f; setTextColor(Color.rgb(135, 160, 190))
            setPadding(0, 4, 0, 14)
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
            hint = "Paste your code here…"
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setBackgroundColor(Color.rgb(29, 36, 47))
            setPadding(14, 14, 14, 14)
            minLines = 9
        }
        root.addView(editor, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f).apply { setMargins(0, 12, 0, 12) })
        root.addView(TextView(this).apply { text = "PREVIEW"; setTextColor(Color.rgb(100, 180, 255)); textSize = 11f })
        preview = TextView(this).apply {
            text = "Your animated code preview appears here."
            setTextColor(Color.LTGRAY); typeface = android.graphics.Typeface.MONOSPACE; textSize = 12f
            setBackgroundColor(Color.rgb(29, 36, 47)); setPadding(12, 12, 12, 12)
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, 100).apply { setMargins(0, 6, 0, 10) })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val previewButton = Button(this).apply { text = "Preview" }
        previewButton.setOnClickListener { preview.text = editor.text.toString().take(600) }
        val exportButton = Button(this).apply { text = "Export MP4" }
        exportButton.setOnClickListener { exportVideo() }
        actions.addView(previewButton, LinearLayout.LayoutParams(0, 54, 1f))
        actions.addView(exportButton, LinearLayout.LayoutParams(0, 54, 1f))
        root.addView(actions)
        status = TextView(this).apply { text = "Ready • Select code, then preview or export."; setTextColor(Color.rgb(160, 180, 200)); textSize = 12f; setPadding(0, 8, 0, 0) }
        root.addView(status)
        setContentView(root)
    }

    private fun exportVideo() {
        val source = editor.text.toString()
        if (source.isBlank()) { status.text = "Masukkan kode terlebih dahulu."; return }
        status.text = "Rendering video…"
        Thread {
            try {
                val file = CodeVideoRenderer(this).render(source, language, resolution)
                runOnUiThread { status.text = "Selesai: " + file.absolutePath }
            } catch (e: Exception) {
                runOnUiThread { status.text = "Export gagal: " + (e.message ?: "unknown error") }
            }
        }.start()
    }
}