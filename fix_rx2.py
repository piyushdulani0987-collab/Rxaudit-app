import re

with open('app/src/main/java/com/example/ui/screens/PrescriptionScanScreen.kt', 'r') as f:
    content = f.read()

sequence = processor.batch_decode(outputs.sequences)[0]
sequence = sequence.replace(processor.tokenizer.eos_token, "")
sequence = re.sub(r"<.*?>", "", sequence, count=1).strip()
print(processor.token2json(sequence))
data = processor.token2json(sequence)
menu_items = data.get("menu", [])
medicines = []
for item in menu_items:
  nm = item.get("nm", "")
  unitprice = item.get("unitprice", "")
  cnt = item.get("cnt", "")
  medicines.append(f"{nm} (Price: {unitprice}, Qty: {cnt})")
final_text = "\n".join(medicines)
content = content.replace("viewModel.setRxTextInput(\"\")", f"viewModel.setRxTextInput(\"{final_text}\")")
content = content.replace("viewModel.setRxTextInput(it)", f"viewModel.setRxTextInput(it)")
content = content.replace("viewModel.setRxTextInput(\"Prescription image attached for Multimodal AI OCR.\")", f"viewModel.setRxTextInput(\"{final_text}\")")
content = content.replace("viewModel.setRxTextInput(\"\")", f"viewModel.setRxTextInput(it)")
content = content.replace("viewModel.setRxTextInput(\"\")", f"viewModel.setRxTextInput(\"\")")

content = content.replace('.trimIndent())', '.trimIndent()')
content = content.replace('viewModel.setSelectedImageUri(null)', 'selectedImageUri = null')

with open('app/src/main/java/com/example/ui/screens/PrescriptionScanScreen.kt', 'w') as f:
    f.write(content)
