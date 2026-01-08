/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.log.taggedLogger
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import kotlin.math.pow
import kotlin.math.sqrt

@InternalStreamChatApi
public class WaveformExtractor(
    private val context: Context,
    private val key: String,
    private val expectedPoints: Int,
    private val extractorCallBack: ExtractorCallBack,
) {

    private val logger by taggedLogger("WaveformExtractor")

    private var decoder: MediaCodec? = null
    private var extractor: MediaExtractor? = null
    private var durationInSeconds = 0f
    private var progress = 0F
    private var currentProgress = 0F

    @Volatile
    private var started = false
    private val finishCount = CountDownLatch(1)
    private var inputEof = false
    private var sampleRate = 0
    private var channels = 1
    private var pcmEncodingBit = 16
    private var totalSamples = 0L
    private var samplesPerPoint = 0L

    private fun getFormat(path: String): MediaFormat? {
        val mediaExtractor = MediaExtractor()
        this.extractor = mediaExtractor
        val uri = Uri.parse(path)
        mediaExtractor.setDataSource(context, uri, null)
        val trackCount = mediaExtractor.trackCount
        repeat(trackCount) {
            val format = mediaExtractor.getTrackFormat(it)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.contains("audio")) {
                durationInSeconds = format.getLong(MediaFormat.KEY_DURATION) / 1_000_000f
                mediaExtractor.selectTrack(it)
                return format
            }
        }
        return null
    }

    public fun start(path: String) {
        try {
            logger.i { "[start] started: $started, path: $path" }
            if (started) return
            started = true
            val format = getFormat(path) ?: error("No audio format found")
            val mime = format.getString(MediaFormat.KEY_MIME) ?: error("No MIME type found")
            decoder = MediaCodec.createDecoderByType(mime).also {
                it.configure(format, null, null, 0)
                it.setCallback(object : MediaCodec.Callback() {
                    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                        if (inputEof) return
                        val extractor = extractor ?: return
                        codec.getInputBuffer(index)?.let { buf ->
                            val size = extractor.readSampleData(buf, 0)
                            if (size > 0) {
                                codec.queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
                                extractor.advance()
                            } else {
                                codec.queueInputBuffer(
                                    index,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                                )
                                inputEof = true
                            }
                        }
                    }

                    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        pcmEncodingBit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                            format.containsKey(MediaFormat.KEY_PCM_ENCODING)
                        ) {
                            when (format.getInteger(MediaFormat.KEY_PCM_ENCODING)) {
                                AudioFormat.ENCODING_PCM_16BIT -> 16
                                AudioFormat.ENCODING_PCM_8BIT -> 8
                                AudioFormat.ENCODING_PCM_FLOAT -> 32
                                else -> 16
                            }
                        } else {
                            16
                        }
                        totalSamples = (sampleRate * durationInSeconds).toLong()
                        samplesPerPoint = totalSamples / expectedPoints
                    }

                    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                        // TODO notify Error
                        finishCount.countDown()
                    }

                    override fun onOutputBufferAvailable(
                        codec: MediaCodec,
                        index: Int,
                        info: MediaCodec.BufferInfo,
                    ) {
                        if (info.size > 0) {
                            codec.getOutputBuffer(index)?.let { buf ->
                                val size = info.size
                                buf.position(info.offset)
                                when (pcmEncodingBit) {
                                    8 -> {
                                        handle8bit(size, buf)
                                    }

                                    16 -> {
                                        handle16bit(size, buf)
                                    }

                                    32 -> {
                                        handle32bit(size, buf)
                                    }
                                }
                                codec.releaseOutputBuffer(index, false)
                            }
                        }

                        if (info.isEof()) {
                            stop()
                        }
                    }
                })
                it.start()
            }
        } catch (e: Exception) {
            // TODO notify Error
        }
    }

    public var sampleData: ArrayList<Float> = ArrayList()
    private var sampleCount = 0L
    private var squaredSampleSum = 0.0f

    private fun rms(sample: Float) {
        if (sampleCount == samplesPerPoint) {
            currentProgress++
            progress = currentProgress / expectedPoints

            // Discard redundant values and release resources
            if (progress > 1.0F) {
                stop()
                return
            }
            val averageSquaredSample = squaredSampleSum / samplesPerPoint
            val rms = sqrt(averageSquaredSample)
            logger.v { "[rms] sample: $sample, averageSquaredSample: $averageSquaredSample, rms: $rms" }
            sampleData.add(rms)
            extractorCallBack.onProgress(this, progress)
            sampleCount = 0
            squaredSampleSum = 0.0f

            val args: MutableMap<String, Any?> = HashMap()
            args["waveformData"] = sampleData
            args["progress"] = progress
            args["playerKey"] = key
            // TODO notify success
            // logger.v { "[rms] sampleData: $sampleData" }
        }

        sampleCount++
        squaredSampleSum += sample.pow(x = 2f)
    }

    private fun handle8bit(size: Int, buf: ByteBuffer) {
        logger.v { "[handle8bit] size: $size" }
        repeat(size / if (channels == 2) 2 else 1) {
            val result = buf.get().toInt() / 127f
            if (channels == 2) {
                buf.get()
            }
            rms(result)
        }
    }

    private fun handle16bit(size: Int, buf: ByteBuffer) {
        logger.v { "[handle16bit] size: $size" }
        logger.v { "[handle16bit] this.totalSamples: $totalSamples, sampleRate: $sampleRate, duration: $durationInSeconds" }
        repeat(size / if (channels == 2) 4 else 2) {
            val first = buf.get().toInt()
            val second = buf.get().toInt() shl 8
            val amplitude = first or second
            logger.v { "[handle16bit] amplitude: $amplitude" }
            val sample = amplitude / 32767f
            if (channels == 2) {
                buf.get()
                buf.get()
            }
            rms(sample)
        }
    }

    private fun handle32bit(size: Int, buf: ByteBuffer) {
        logger.v { "[handle32bit] size: $size" }
        repeat(size / if (channels == 2) 8 else 4) {
            val first = buf.get().toLong()
            val second = buf.get().toLong() shl 8
            val third = buf.get().toLong() shl 16
            val forth = buf.get().toLong() shl 24
            val value = (first or second or third or forth) / 2_147_483_648f
            if (channels == 2) {
                buf.get()
                buf.get()
                buf.get()
                buf.get()
            }
            rms(value)
        }
    }

    public fun stop() {
        logger.i { "[stop] started: $started" }
        if (!started) return
        started = false
        decoder?.stop()
        decoder?.release()
        extractor?.release()
        finishCount.countDown()
    }
}

public fun MediaCodec.BufferInfo.isEof(): Boolean = flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0

@InternalStreamChatApi
public fun interface ExtractorCallBack {
    public fun onProgress(extractor: WaveformExtractor, value: Float)
}
