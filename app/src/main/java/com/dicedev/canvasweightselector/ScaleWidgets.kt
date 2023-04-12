package com.dicedev.canvasweightselector

import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withRotation
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun Scale(
    modifier: Modifier = Modifier,
    style: ScaleStyle = ScaleStyle(),
    minWeight: Int = 20,
    maxWeight: Int = 250,
    initialWeight: Int = 80,
    onWeightChange: (Int) -> Unit
) {
    val radius = style.radius
    val scaleWidth = style.scaleWidth

    var center by remember {
        mutableStateOf(Offset.Zero)
    }

    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var angle by remember {
        mutableStateOf(0f)
    }

    var dragStartedAngle by remember {
        mutableStateOf(0f)
    }

    var oldAngle by remember {
        mutableStateOf(angle)
    }

    Canvas(modifier = modifier.pointerInput(key1 = true) {
        detectDragGestures(
            onDragStart = { offset ->
                dragStartedAngle = -atan2(
                    y = circleCenter.x - offset.x,
                    x = circleCenter.y - offset.y
                ) * (180f / PI.toFloat())
            },
            onDragEnd = {
                oldAngle = angle
            }
        ) { change, _ ->
            val touchAngle = -atan2(
                y = circleCenter.x - change.position.x,
                x = circleCenter.y - change.position.y
            ) * (180f / PI.toFloat())

            val newAngle = oldAngle + (touchAngle - dragStartedAngle)
            angle = newAngle.coerceIn(
                minimumValue = initialWeight - maxWeight.toFloat(),
                maximumValue = initialWeight - minWeight.toFloat()
            )

            onWeightChange((initialWeight - angle).roundToInt())
        }
    }) {
        center = this.center
        circleCenter = Offset(center.x, scaleWidth.toPx() / 2f + radius.toPx())
        val outerRadius = radius.toPx() + scaleWidth.toPx() / 2f
        val innerRadius = radius.toPx() - scaleWidth.toPx() / 2f

        drawContext.canvas.nativeCanvas.apply {
            drawCircle(circleCenter.x, circleCenter.y, radius.toPx(), Paint().apply {
                strokeWidth = scaleWidth.toPx()
                color = Color.WHITE
                setStyle(Paint.Style.STROKE)
                setShadowLayer(60f, 0f, 0f, Color.argb(50, 0, 0, 0))
            })
        }

        // Draw Lines

        for (weight in minWeight..maxWeight) {
            val angleInRad = (weight - initialWeight + angle - 90) * (PI / 180f).toFloat()
            val lineType = when {
                weight % 10 == 0 -> LineType.TenStep
                weight % 5 == 0 -> LineType.FiveStep
                else -> LineType.Normal
            }
            val lineLength = when (lineType) {
                LineType.Normal -> style.normalLineLength.toPx()
                LineType.FiveStep -> style.fiveStepLineLength.toPx()
                LineType.TenStep -> style.tenStepLineLength.toPx()
            }
            val lineColor = when (lineType) {
                LineType.Normal -> style.normalLineColor
                LineType.FiveStep -> style.fiveStepLineColor
                LineType.TenStep -> style.tenStepLineColor
            }
            val lineStart = Offset(
                x = ((outerRadius - lineLength) * kotlin.math.cos(angleInRad.toDouble()) + circleCenter.x).toFloat(),
                y = ((outerRadius - lineLength) * kotlin.math.sin(angleInRad.toDouble()) + circleCenter.y).toFloat()
            )
            val lineEnd = Offset(
                x = (outerRadius * kotlin.math.cos(angleInRad) + circleCenter.x),
                y = (outerRadius * kotlin.math.sin(angleInRad) + circleCenter.y)
            )

            drawContext.canvas.nativeCanvas.apply {
                if (lineType is LineType.TenStep) {
                    val textRadius = outerRadius - lineLength - 5.dp.toPx() - style.textSize.toPx()
                    val x = textRadius * kotlin.math.cos(angleInRad) + circleCenter.x
                    val y = textRadius * kotlin.math.sin(angleInRad) + circleCenter.y

                    withRotation(
                        degrees = angleInRad * (180f / PI.toFloat()) + 90f,
                        pivotX = x,
                        pivotY = y
                    ) {
                        drawText(
                            abs(weight).toString(),
                            x,
                            y,
                            Paint().apply {
                                textSize = style.textSize.toPx()
                                textAlign = Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
            drawLine(color = lineColor, start = lineStart, end = lineEnd, strokeWidth = 1.dp.toPx())
        }

        val middleTop = Offset(
            x = circleCenter.x,
            y = circleCenter.y - innerRadius - style.scaleIndicatorLength.toPx()
        )
        val bottomLeft = Offset(
            x = circleCenter.x - 4f,
            y = circleCenter.y - innerRadius
        )
        val bottomRight = Offset(
            x = circleCenter.x + 4f,
            y = circleCenter.y - innerRadius
        )
        val indicator = Path().apply {
            moveTo(middleTop.x, middleTop.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            lineTo(bottomRight.x, bottomRight.y)
            close()
        }

        drawPath(path = indicator, color = style.scaleIndicatorColor)


    }
}