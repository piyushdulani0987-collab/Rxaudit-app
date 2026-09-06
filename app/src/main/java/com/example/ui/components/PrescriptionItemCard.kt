package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrescriptionEntity
import com.example.ui.theme.CompliantGreen
import com.example.ui.theme.NonCompliantRed
import com.example.ui.theme.NonCompliantRedBg
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrescriptionItemCard(
    prescription: PrescriptionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("prescription_card_${prescription.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Rx No, Department, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prescription.rxNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prescription.department,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                StatusBadge(isCompliant = prescription.isCompliantOverall)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Patient Info & Diagnosis
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${prescription.patientName}, ${prescription.patientAge}y (${prescription.patientGender.take(1)})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = prescription.uhid,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Diagnosis
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (prescription.diagnosis.isNotBlank()) prescription.diagnosis else "Diagnosis Missing",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (prescription.diagnosis.isNotBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else NonCompliantRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Doctor & Drugs Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${prescription.doctorName} ${if (prescription.doctorRegNumber.isNotBlank()) "(${prescription.doctorRegNumber})" else "[Reg Missing]"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = if (prescription.doctorRegNumber.isNotBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else NonCompliantRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${prescription.totalDrugsCount} Drugs (${prescription.genericCount} Gen)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Semantic Tags Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (prescription.scheduleH1Count > 0) {
                    ScheduleH1Badge(count = prescription.scheduleH1Count)
                }
                if (prescription.antibioticCount > 0) {
                    AntibioticBadge(awarClass = "Antibiotic")
                }
                if (prescription.isPolypharmacy) {
                    PolypharmacyBadge(count = prescription.totalDrugsCount)
                }
                if (prescription.irrationalFdcCount > 0) {
                    Text(
                        text = "Irrational FDC Flagged",
                        color = NonCompliantRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NonCompliantRedBg)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Deficiencies Snippet if present
            if (prescription.deficienciesList.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deficiencies: ${prescription.deficienciesList}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = NonCompliantRed,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(NonCompliantRedBg.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Scores & Hash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HashChainBadge(docHash = prescription.docHash)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Comp: ${prescription.completenessScorePct}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (prescription.completenessScorePct >= 85) CompliantGreen else WarningAmber
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rat: ${prescription.rationalityScorePct}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (prescription.rationalityScorePct >= 80) CompliantGreen else WarningAmber
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
