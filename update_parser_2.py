import re

with open('app/src/main/java/com/example/domain/PrescriptionOcrParser.kt', 'r') as f:
    content = f.read()

replacement1 = """                            frequency = d.optString("frequency", "BD"),
                            durationDays = d.optInt("durationDays", 5),
                            instructions = d.optString("instructions", "")"""

content = re.sub(
    r'frequency = d\.optString\("frequency", "BD"\),\s*durationDays = d\.optInt\("durationDays", 5\)',
    replacement1,
    content
)

with open('app/src/main/java/com/example/domain/PrescriptionOcrParser.kt', 'w') as f:
    f.write(content)
