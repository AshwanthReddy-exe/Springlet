#!/bin/bash

set -e

echo "🚀 Installing Springlet..."

JAR_URL="https://github.com/AshwanthReddy-exe/Springlet/releases/download/v1.0/spring-cli-1.0.jar"

INSTALL_DIR="/usr/local/lib"
BIN_DIR="/usr/local/bin"

# Check Java
if ! command -v java &>/dev/null; then
  echo "❌ Java not found. Install Java 17+ first."
  exit 1
fi

# Check existing install
if [ -f "$BIN_DIR/springlet" ]; then
  echo "⚠️ Springlet already installed. Updating..."
fi

# Download jar
echo "📦 Downloading..."
curl -L "$JAR_URL" -o springlet.jar

# Install jar
echo "📁 Installing..."
sudo mv springlet.jar $INSTALL_DIR/
sudo chmod +x $INSTALL_DIR/springlet.jar

# Create command
echo "⚙️ Creating command..."
echo '#!/bin/bash' | sudo tee $BIN_DIR/springlet >/dev/null
echo 'java -jar /usr/local/lib/springlet.jar "$@"' | sudo tee -a $BIN_DIR/springlet >/dev/null

sudo chmod +x $BIN_DIR/springlet

echo "✅ Installed successfully!"
echo "👉 Run: springlet init"
