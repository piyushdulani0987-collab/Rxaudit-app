import re

with open('app/src/main/java/com/example/domain/PrescriptionOcrParser.kt', 'r') as f:
    content = f.read()

# Add to text prompt
content = re.sub(
    r'"durationDays": 5\n\s*\}',
    r'"durationDays": 5,\n                      "instructions": "string (e.g. After food, Before food)"\n                    }',
    content
)

# Parse from JSON
content = re.sub(
    r'val duration = dObj\.optInt\("durationDays", 5\)',
    r'val duration = dObj.optInt("durationDays", 5)\n                        val instructions = dObj.optString("instructions", "")',
    content
)

# Update evaluateDrug call in the JSON parsing block
content = re.sub(
    r'AuditRulesEngine\.evaluateDrug\(brandName, dose, route, frequency, duration\)',
    r'AuditRulesEngine.evaluateDrug(brandName, dose, route, frequency, duration, instructions)',
    content
)

with open('app/src/main/java/com/example/domain/PrescriptionOcrParser.kt', 'w') as f:
    f.write(content)
