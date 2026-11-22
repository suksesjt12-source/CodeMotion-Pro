package com.codemotion.pro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File

class CodeVideoRenderer(private val context: Context) {
    fun render(code: String, language: String, size: String): File {
        val width = if (size == "1080p") 1920 else 1280
        val height = if (size == "1080p") 1080 else 720
        val fps = 24
        val frameCount = fps * 8
        val output = File(context.getExternalFilesDir(null), "CodeMotion-" + System.currentTimeMillis() + ".mp4")
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, if (size == "1080p") 5_000_000 else 2_500_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = encoder.createInputSurface()
        val muxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        encoder.start()
        var trackIndex = -1
        var muxerStarted = false
        val info = MediaCodec.BufferInfo()
        try {
            val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(225, 232, 242); textSize = 30f; typeface = android.graphics.Typeface.MONOSPACE }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(90, 180, 255); textSize = 25f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(90, 105, 125); textSize = 23f; typeface = android.graphics.Typeface.MONOSPACE }
            val panelPaint = Paint().apply { color = Color.rgb(30, 38, 51) }
            for (frame in 0 until frameCount) {
                val canvas: Canvas = surface.lockCanvas(null)
                try {
                    canvas.drawColor(Color.rgb(13, 17, 23))
                    canvas.drawRoundRect(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat(), 20f, 20f, panelPaint)
                    canvas.drawText("CodeMotion Pro  /  " + language, 75f, 95f, titlePaint)
                    val visible = ((code.length.coerceAtLeast(1).toLong() * (frame + 1)) / frameCount).toInt().coerceAtLeast(1)
                    code.take(visible).split("\n").take(((height - 180) / 40).coerceAtLeast(1)).forEachIndexed { index, line ->
                        val y = 165f + index * 40f
                        canvas.drawText((index + 1).toString().padStart(2, '0'), 75f, y, linePaint)
                        canvas.drawText(line.take(100), 135f, y, codePaint)
                    }
                    canvas.drawText("Generated offline  •  " + size, 75f, (height - 75).toFloat(), titlePaint)
                } finally {
                    surface.unlockCanvasAndPost(canvas)
                }
                // Keep frame timestamps close to the selected 24 FPS output cadence.
                Thread.sleep(1000L / fps)
                val state = drain(encoder, muxer, info, trackIndex, muxerStarted, false)
                trackIndex = state.first
                muxerStarted = state.second
            }
            encoder.signalEndOfInputStream()
            var finished = false
            while (!finished) {
                val state = drain(encoder, muxer, info, trackIndex, muxerStarted, true)
                trackIndex = state.first
                muxerStarted = state.second
                finished = state.third
            }
        } finally {
            try { encoder.stop() } catch (_: Exception) {}
            encoder.release()
            surface.release()
            if (muxerStarted) muxer.stop()
            muxer.release()
        }
        return output
    }

    private fun drain(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        info: MediaCodec.BufferInfo,
        initialTrack: Int,
        initiallyStarted: Boolean,
        waitForEnd: Boolean
    ): Triple<Int, Boolean, Boolean> {
        var track = initialTrack
        var started = initiallyStarted
        var eos = false
        while (true) {
            val index = encoder.dequeueOutputBuffer(info, if (waitForEnd) 10_000 else 0)
            when {
                index == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!waitForEnd) return Triple(track, started, eos)
                    continue
                }
                index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    track = muxer.addTrack(encoder.outputFormat)
                    muxer.start()
                    started = true
                }
                index >= 0 -> {
                    val buffer = encoder.getOutputBuffer(index)
                    if (buffer != null && info.size > 0 && started) {
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        muxer.writeSampleData(track, buffer, info)
                    }
                    eos = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                    encoder.releaseOutputBuffer(index, false)
                    if (eos) return Triple(track, started, true)
                }
            }
        }
    }
}