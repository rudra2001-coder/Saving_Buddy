package com.rudra.savingbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.util.CurrencyFormatter

@Composable
fun PieChart(
    data: List<CategoryTotal>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.total }
    if (total <= 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            var startAngle = -90f
            data.forEach { item ->
                val sweepAngle = (item.total / total * 360f).toFloat()
                drawArc(
                    color = getCategoryColor(item.category),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweepAngle
            }

            drawCircle(
                color = surfaceColor,
                radius = size.width / 4
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = getCategoryColor(item.category))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(item.total / total * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { index, pair ->
            Offset(
                x = index * stepX,
                y = size.height - (pair.second / maxValue * size.height).toFloat()
            )
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }

        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = point
            )
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second } ?: 1.0

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val barWidth = size.width / data.size * 0.7f
            val spacing = size.width / data.size

            data.forEachIndexed { index, pair ->
                val barHeight = (pair.second / maxValue * size.height).toFloat()
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(index * spacing + spacing / 2 - barWidth / 2, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.take(6).forEach { (label, _) ->
                Text(
                    text = label.take(3),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun CategoryBarChart(
    data: List<CategoryTotal>,
    modifier: Modifier = Modifier,
    maxBars: Int = 10
) {
    val sortedData = data.sortedByDescending { it.total }.take(maxBars)
    val total = data.sumOf { it.total }
    
    if (sortedData.isEmpty() || total <= 0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }
    
    val maxValue = sortedData.maxOfOrNull { it.total } ?: 1.0
    
    Column(modifier = modifier) {
        sortedData.forEach { item ->
            val percentage = if (total > 0) (item.total / total * 100) else 0.0
            val barProgress = (item.total / maxValue).toFloat()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.category.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(80.dp)
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = getCategoryColor(item.category).copy(alpha = 0.3f),
                            size = Size(size.width, size.height)
                        )
                        drawRoundRect(
                            color = getCategoryColor(item.category),
                            size = Size(size.width * barProgress, size.height)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.format(item.total),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "FOOD" -> FoodColor
        "TRANSPORT" -> TransportColor
        "BILLS" -> BillsColor
        "SHOPPING" -> ShoppingColor
        else -> OthersColor
    }
}