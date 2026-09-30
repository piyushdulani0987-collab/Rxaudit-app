import re

with open('app/src/main/java/com/example/ui/screens/PrescriptionScanScreen.kt', 'r') as f:
    content = f.read()

replacement = """                        Text(
                            text = "Dosage: ${drug.dose} • ${drug.route} • ${drug.frequency} for ${drug.durationDays} days",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        if (drug.instructions.isNotBlank()) {
                            Text(
                                text = "Usage Instructions: ${drug.instructions}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                        }"""

content = re.sub(
    r'Text\(\s*text = "Dosage: \$\{drug\.dose\}.*?alpha = 0\.8f\)\s*\)',
    replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/PrescriptionScanScreen.kt', 'w') as f:
    f.write(content)
