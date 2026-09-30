package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.NonCompliantRed

@Composable
fun CompliancePieChart(
    compliantCount: Int,
    nonCompliantCount: Int,
    modifier: Modifier = Modifier
) {
    val total = compliantCount + nonCompliantCount
    val compliantAngle = if (total == 0) 0f else (compliantCount.toFloat() / total) * 360f
    
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(120.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Draw background circle (Non-compliant red)
            drawArc(
                color = NonCompliantRed,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw compliant arc (Green)
            if (compliantAngle > 0f) {
                drawArc(
                    color = CompliantGreen,
                    startAngle = -90f,
                    sweepAngle = compliantAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val pct = if (total == 0) 0 else (compliantCount * 100) / total
            Text(text = "$pct%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Compliant", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
