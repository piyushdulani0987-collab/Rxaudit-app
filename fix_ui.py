import re

with open('app/src/main/java/com/example/ui/screens/PrescriptionScanScreen.kt', 'r') as f:
    content = f.read()

# Replace assignments: rxTextInput = """...""".trimIndent()
content = re.sub(r'rxTextInput\s*=\s*"""(.*?)"""\.trimIndent\(\)', r'viewModel.setRxTextInput("""\1""".trimIndent())', content, flags=re.DOTALL)

# Other assignments
content = re.sub(r'rxTextInput\s*=\s*""', r'viewModel.setRxTextInput("")', content)
content = re.sub(r'rxTextInput\s*=\s*"Prescription image attached for Multimodal AI OCR."', r'viewModel.setRxTextInput("Prescription image attached for Multimodal AI OCR.")', content)
content = re.sub(r'rxTextInput\s*=\s*it', r'viewModel.setRxTextInput(it)', content)

# selectedImageUri
content = re.sub(r'selectedImageUri\s*=\s*uri', r'viewModel.setSelectedImageUri(uri)', content)
content = re.sub(r'selectedImageUri\s*=\s*null', r'viewModel.setSelectedImageUri(null)', content)

with open('app/src/main/java/com/example/ui/screens/PrescriptionScanScreen.kt', 'w') as f:
    f.write(content)
