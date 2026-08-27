with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

count = 0
for i, line in enumerate(lines):
    count += line.count('{')
    count -= line.count('}')
    if count == 0 and 'class MainActivity' not in line and i > 100:
        print(f"Braces reached zero at line {i+1}: {line.strip()}")
