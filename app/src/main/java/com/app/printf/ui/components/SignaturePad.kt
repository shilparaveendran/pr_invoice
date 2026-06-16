package com.app.printf.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

class SignaturePadController internal constructor(
    internal val strokes: SnapshotStateList<List<Offset>>,
) {
    internal var canvasSize: IntSize = IntSize.Zero

    fun clear() {
        strokes.clear()
    }

    fun hasContent(): Boolean = strokes.isNotEmpty()

    fun toBitmap(): Bitmap? {
        if (!hasContent() || canvasSize.width <= 0 || canvasSize.height <= 0) return null
        val bitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val strokeWidth = 4f * (canvasSize.width / 400f).coerceAtLeast(1f)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
            isAntiAlias = true
        }
        strokes.forEach { stroke ->
            if (stroke.isEmpty()) return@forEach
            val path = android.graphics.Path()
            path.moveTo(stroke.first().x, stroke.first().y)
            stroke.drop(1).forEach { point ->
                path.lineTo(point.x, point.y)
            }
            canvas.drawPath(path, paint)
        }
        return bitmap
    }
}

@Composable
fun rememberSignaturePadController(): SignaturePadController {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    return remember(strokes) { SignaturePadController(strokes) }
}

@Composable
fun SignaturePad(
    controller: SignaturePadController,
    modifier: Modifier = Modifier,
) {
    val strokeStyle = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val currentStroke = remember { mutableStateListOf<Offset>() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .onSizeChanged { controller.canvasSize = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentStroke.clear()
                        currentStroke.add(offset)
                    },
                    onDrag = { change, _ ->
                        currentStroke.add(change.position)
                    },
                    onDragEnd = {
                        if (currentStroke.isNotEmpty()) {
                            controller.strokes.add(currentStroke.toList())
                        }
                        currentStroke.clear()
                    },
                )
            },
    ) {
        fun drawStroke(points: List<Offset>) {
            if (points.isEmpty()) return
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, Color.Black, style = strokeStyle)
        }

        controller.strokes.forEach { stroke -> drawStroke(stroke) }
        drawStroke(currentStroke.toList())
    }
}
