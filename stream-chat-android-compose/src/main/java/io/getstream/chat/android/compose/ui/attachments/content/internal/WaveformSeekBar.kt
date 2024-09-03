package io.getstream.chat.android.compose.ui.attachments.content.internal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import kotlin.random.Random

@Composable
public fun WaveformSeekBar(
    waveform: List<Float>,
    progress: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(progress) }
    val width = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newPosition = (offset.x / width.floatValue).coerceIn(0f, 1f)
                    sliderPosition = newPosition
                    onValueChange(sliderPosition)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newPosition = (sliderPosition + dragAmount.x / width.floatValue)
                        .coerceIn(0f, 1f)
                    sliderPosition = newPosition
                    onValueChange(sliderPosition)
                }
            }
            .onSizeChanged { size ->
                width.floatValue = size.width.toFloat()
            }
    ) {
        // Draw the waveform
        Waveform(
            waveform = waveform,
            progress = sliderPosition,
            modifier = Modifier.fillMaxSize()
        )

        // Draw the thumb

        if (width.floatValue > 0) {
            val density = LocalDensity.current
            val offsetX = with(density) {
                (width.floatValue * sliderPosition).toDp() - 8.dp
            }.coerceIn(0.dp, with(density) { (width.floatValue - 16.dp.toPx()).toDp() })
            Box(

                modifier = Modifier
                    .offset(x = offsetX)
                    .size(16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .align(Alignment.CenterStart)
            )
        }
    }
}



@Composable
internal fun Waveform(
    waveform: List<Float>,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / (waveform.size)
        waveform.forEachIndexed { index, amplitude ->
            drawRect(
                color = if (index <= (progress * waveform.size).toInt()) Color.Gray else Color.LightGray,
                topLeft = Offset(
                    x = index * barWidth,
                    y = (size.height - amplitude * size.height) / 2
                ),
                size = Size(
                    width = barWidth,
                    height = amplitude * size.height
                )
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
internal fun WaveformSeekBarPreview() {
    val rand = Random(100)
    val waveform = mutableListOf<Float>()
    for (i in 0..100) {
        waveform.add(rand.nextFloat())
    }


    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(60.dp)
                .background(Color.Cyan),
            contentAlignment = Alignment.Center
        ) {
            WaveformSeekBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                waveform = waveform,
                progress = 1.0f,
                onValueChange = {},
            )
        }
    }
}