package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AWaReAccess
import com.example.ui.theme.AWaReReserve
import com.example.ui.theme.AWaReWatch
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.CompliantGreenBg
import com.example.ui.theme.NonCompliantRed
import com.example.ui.theme.NonCompliantRedBg
import com.example.ui.theme.ScheduleHBg
import com.example.ui.theme.ScheduleHIndigo
import com.example.ui.theme.ScheduleH1Bg
import com.example.ui.theme.ScheduleH1Orange
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg

@Composable
fun StatusBadge(isCompliant: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isCompliant) CompliantGreenBg else NonCompliantRedBg
    val color = if (isCompliant) CompliantGreen else NonCompliantRed
    val text = if (isCompliant) "NABH COMPLIANT" else "NON-COMPLIANT"
    val icon = if (isCompliant) Icons.Default.CheckCircle else Icons.Default.Error

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScheduleH1Badge(count: Int = 1, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ScheduleH1Bg)
            .border(1.dp, ScheduleH1Orange.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red line strip mimicking India Schedule H1 packaging mandate
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 10.dp)
                .background(Color.Red, RoundedCornerShape(1.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (count > 1) "Schedule H1 ($count)" else "Schedule H1",
            color = ScheduleH1Orange,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScheduleHBadge(modifier: Modifier = Modifier) {
    Text(
        text = "Schedule H (Rx)",
        color = ScheduleHIndigo,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ScheduleHBg)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
fun AntibioticBadge(awarClass: String, modifier: Modifier = Modifier) {
    val (color, text) = when (awarClass.lowercase()) {
        "access" -> Pair(AWaReAccess, "AWaRe: Access")
        "watch" -> Pair(AWaReWatch, "AWaRe: Watch")
        "reserve" -> Pair(AWaReReserve, "AWaRe: Reserve")
        else -> Pair(AWaReWatch, "Antibiotic")
    }

    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
fun PolypharmacyBadge(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(WarningAmberBg)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "Polypharmacy ($count)",
            color = WarningAmber,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HashChainBadge(docHash: String, isValid: Boolean = true, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            tint = if (isValid) CompliantGreen else NonCompliantRed,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "SHA-256: ${docHash.take(8)}...",
            color = Color(0xFF475569),
            fontSize = 9.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
