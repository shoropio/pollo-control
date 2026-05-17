package com.pollocontrol.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MiniLineChart(
    dataPoints: List<Float>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (dataPoints.size < 2) return

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Tendencia",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            val surfaceColor = MaterialTheme.colorScheme.surface
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val strokeWidth = 2.dp.toPx()
                val padding = 16.dp.toPx()
                val chartWidth = size.width - padding * 2
                val chartHeight = size.height - padding * 2

                val min = dataPoints.min()
                val max = dataPoints.max()
                val range = if (max - min == 0f) 1f else max - min

                val points = dataPoints.mapIndexed { i, v ->
                    val x = padding + (i.toFloat() / (dataPoints.size - 1)) * chartWidth
                    val y = padding + chartHeight - ((v - min) / range) * chartHeight
                    Offset(x, y)
                }

                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height - padding)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height - padding)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.2f), lineColor.copy(alpha = 0.0f)),
                        endY = size.height - padding
                    )
                )

                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                points.forEach { p ->
                    drawCircle(color = lineColor, radius = 3.dp.toPx(), center = p)
                    drawCircle(
                        color = surfaceColor,
                        radius = 1.5.dp.toPx(),
                        center = p
                    )
                }
            }
        }
    }
}

@Composable
fun MiniBarChart(
    dataPoints: List<Float>,
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Distribucion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val padding = 12.dp.toPx()
                val chartWidth = size.width - padding * 2
                val chartHeight = size.height - padding * 2
                val barWidth = chartWidth / dataPoints.size * 0.7f
                val gap = chartWidth / dataPoints.size * 0.3f
                val maxVal = dataPoints.maxOrNull() ?: 1f

                dataPoints.forEachIndexed { i, v ->
                    val barHeight = (v / maxVal) * chartHeight
                    val x = padding + i * (barWidth + gap)
                    val y = padding + chartHeight - barHeight

                    drawRoundRect(
                        color = barColor.copy(alpha = 0.7f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }
    }
}
