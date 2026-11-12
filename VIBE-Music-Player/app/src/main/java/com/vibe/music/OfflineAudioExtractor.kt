package com.vibe.music

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

object OfflineAudioExtractor {
    /** Offline stream-copy extraction. Supports AAC audio tracks and writes M4A; no re-encoding. */
    fun extractM4a(context: Context, source: Uri): File {
        val out = File(context.getExternalFilesDir(null), "VIBE_Audio_${System.currentTimeMillis()}.m4a")
        val extractor = MediaExtractor(); var muxer: MediaMuxer? = null
        try {
            extractor.setDataSource(context, source, null)
            var sourceTrack = -1; var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) { sourceTrack = i; format = f; break }
            }
            require(sourceTrack >= 0 && format != null) { "No audio track found in this video." }
            require(format!!.getString(MediaFormat.KEY_MIME) == "audio/mp4a-latm") { "This video uses an unsupported audio codec. AAC audio is currently supported." }
            extractor.selectTrack(sourceTrack)
            muxer = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val destTrack = muxer.addTrack(format!!); muxer.start()
            val buffer = ByteBuffer.allocate(1024 * 1024); val info = BufferInfo()
            while (true) {
                buffer.clear(); val size = extractor.readSampleData(buffer, 0); if (size < 0) break
                info.offset = 0; info.size = size; info.presentationTimeUs = extractor.sampleTime; info.flags = extractor.sampleFlags
                muxer.writeSampleData(destTrack, buffer, info); extractor.advance()
            }
            muxer.stop(); return out
        } catch (e: Exception) { out.delete(); throw e }
        finally { try { muxer?.release() } catch (_: Exception) {}; extractor.release() }
    }
}
