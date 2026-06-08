mkdir -p .github/workflows
cat > .github/workflows/build.yml << 'EOF'
name: Build AntiESP

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn package -q
      
      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: AntiESP
          path: target/AntiESP-*.jar
EOF

git add .
git commit -m "Add
