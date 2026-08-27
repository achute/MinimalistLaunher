import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# I will just replace the end of the file
pattern = r"            \)\n        \}\n    \}\n\n    @Deprecated\(\"Deprecated in Java\"\)\n    override fun onActivityResult"

replacement = r"            )\n        }\n    }\n}\n\n    @Deprecated(\"Deprecated in Java\")\n    override fun onActivityResult"

new_content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)
