with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('@Deprecated(\\"Deprecated in Java\\")', '@Deprecated("Deprecated in Java")')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
