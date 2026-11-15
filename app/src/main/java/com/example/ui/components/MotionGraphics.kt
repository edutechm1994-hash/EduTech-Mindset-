package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QuantumMotionBackground(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val bg = MaterialTheme.colorScheme.background
    val ribbonPaths = remember { List(3) { Path() } }

    val infiniteTransition = rememberInfiniteTransition(label = "quantum_bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave_phase",
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_pulse",
    )

    val activePhase = if (animate) phase else 0.8f
    val activePulse = if (animate) pulse else 1.0f

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = bg)

        // Ambient radial glows
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.18f),
                radius = size.minDimension * 0.65f * activePulse,
            ),
            center = Offset(size.width * 0.2f, size.height * 0.18f),
            radius = size.minDimension * 0.65f * activePulse,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondary.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(size.width * 0.84f, size.height * 0.72f),
                radius = size.minDimension * 0.55f * activePulse,
            ),
            center = Offset(size.width * 0.84f, size.height * 0.72f),
            radius = size.minDimension * 0.55f * activePulse,
        )

        // Flowing kinetic energy ribbons
        for (waveIdx in 0..2) {
            val path = ribbonPaths[waveIdx]
            path.reset()
            val yBase = size.height * (0.30f + waveIdx * 0.22f)
            val amp = size.height * (0.045f + waveIdx * 0.015f)
            val freq = 1.4f + waveIdx * 0.4f
            val offsetShift = activePhase + waveIdx * 1.1f

            var x = 0f
            val step = (size.width / 36f).coerceAtLeast(12f)
            while (x <= size.width + step) {
                val norm = x / size.width
                val y = yBase + sin(norm * freq * 2f * PI + offsetShift).toFloat() * amp
                if (x == 0f) path.moveTo(x, y) else path.lineTo(x, y)
                x += step
            }

            val ribbonColor = if (waveIdx % 2 == 0) primary else secondary
            drawPath(
                path = path,
                color = ribbonColor.copy(alpha = 0.18f - waveIdx * 0.03f),
                style = Stroke(width = (2.4f - waveIdx * 0.4f).dp.toPx(), cap = StrokeCap.Round),
            )
        }

        // Orbiting neural constellation nodes
        val nodeCount = 14
        val points = List(nodeCount) { i ->
            val angle = activePhase * 0.5f + i * (2f * PI / nodeCount).toFloat()
            val radiusX = size.width * (0.16f + (i % 4) * 0.09f)
            val radiusY = size.height * (0.14f + (i % 3) * 0.11f)
            Offset(
                x = size.width * 0.5f + cos(angle) * radiusX,
                y = size.height * 0.46f + sin(angle * 1.3f) * radiusY,
            )
        }

        for (i in points.indices) {
            val next = points[(i + 3) % points.size]
            drawLine(
                color = primary.copy(alpha = 0.12f),
                start = points[i],
                end = next,
                strokeWidth = 1.dp.toPx(),
            )
            val dotColor = if (i % 3 == 0) secondary else primary
            drawCircle(
                color = dotColor.copy(alpha = 0.55f),
                radius = (2.5f + (i % 3) * 1.3f).dp.toPx(),
                center = points[i],
            )
        }
    }
}

@Composable
fun AnimatedEduTechCrest(
    modifier: Modifier = Modifier,
    size: Dp = 116.dp,
    animate: Boolean = true,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val infiniteTransition = rememberInfiniteTransition(label = "crest_motion")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit_rotation",
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "crest_pulse",
    )

    val activeRot = if (animate) rotation else 0f
    val activeScale = if (animate) pulseScale else 1f

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerR = this.size.minDimension * 0.46f

            // Outer glowing halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.30f), Color.Transparent),
                    center = center,
                    radius = outerR * 1.15f * activeScale,
                ),
                center = center,
                radius = outerR * 1.15f * activeScale,
            )

            // Rotating dashed orbital ring
            rotate(degrees = activeRot, pivot = center) {
                drawCircle(
                    color = primary.copy(alpha = 0.75f),
                    radius = outerR,
                    center = center,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 14f), 0f),
                        cap = StrokeCap.Round,
                    ),
                )
                // Orbiting gold satellite
                drawCircle(
                    color = secondary,
                    radius = 5.dp.toPx(),
                    center = Offset(center.x + outerR, center.y),
                )
            }

            // Counter-rotating inner hexagon ring
            rotate(degrees = -activeRot * 0.7f, pivot = center) {
                val innerR = outerR * 0.74f
                val hexPath = Path()
                for (i in 0 until 6) {
                    val theta = (i * 60.0) * PI / 180.0
                    val pt = Offset(
                        x = center.x + (innerR * cos(theta)).toFloat(),
                        y = center.y + (innerR * sin(theta)).toFloat(),
                    )
                    if (i == 0) hexPath.moveTo(pt.x, pt.y) else hexPath.lineTo(pt.x, pt.y)
                }
                hexPath.close()
                drawPath(
                    path = hexPath,
                    color = secondary.copy(alpha = 0.75f),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            // Core geometric spark / open book circuit symbol
            val coreW = outerR * 0.56f
            val bookPath = Path().apply {
                moveTo(center.x, center.y + coreW * 0.35f)
                lineTo(center.x - coreW * 0.75f, center.y + coreW * 0.1f)
                lineTo(center.x - coreW * 0.75f, center.y - coreW * 0.45f)
                lineTo(center.x, center.y - coreW * 0.2f)
                lineTo(center.x + coreW * 0.75f, center.y - coreW * 0.45f)
                lineTo(center.x + coreW * 0.75f, center.y + coreW * 0.1f)
                close()
            }
            drawPath(
                path = bookPath,
                brush = Brush.linearGradient(colors = listOf(primary, secondary)),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            // Center upward circuit stem + node
            drawLine(
                color = primary,
                start = Offset(center.x, center.y + coreW * 0.35f),
                end = Offset(center.x, center.y - coreW * 0.72f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = secondary,
                radius = 4.5.dp.toPx(),
                center = Offset(center.x, center.y - coreW * 0.78f),
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave_clock",
    )

    val amplitudeScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.22f,
        animationSpec = spring(stiffness = 220f),
        label = "amp_scale",
    )

    Canvas(modifier = modifier) {
        val barCount = 28
        val totalWidth = size.width
        val slotWidth = totalWidth / barCount
        val barWidth = (slotWidth * 0.56f).coerceAtLeast(3.dp.toPx())
        val midY = size.height / 2f

        for (i in 0 until barCount) {
            val norm = i.toFloat() / barCount
            val envelope = sin(norm * PI).toFloat().coerceAtLeast(0.25f)
            val wave = if (isPlaying) {
                0.35f + 0.65f * ((sin(waveClock * 2f + i * 0.55f) + 1f) / 2f)
            } else {
                0.35f + 0.25f * ((sin(i * 0.7f) + 1f) / 2f)
            }
            val barHeight = (size.height * 0.86f * envelope * wave * amplitudeScale).coerceAtLeast(6.dp.toPx())
            val x = i * slotWidth + (slotWidth - barWidth) / 2f
            val y = midY - barHeight / 2f

            val barColor = if (i % 4 == 0) secondary else primary
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barColor, barColor.copy(alpha = 0.55f))
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

@Composable
fun AnimatedProgressRing(
    progressFraction: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 140f, dampingRatio = 0.75f),
        label = "ring_progress",
    )

    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(size.width - strokePx, size.height - strokePx)

        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )

        drawArc(
            brush = Brush.sweepGradient(colors = listOf(progressColor, secondaryColor, progressColor)),
            startAngle = -90f,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}
