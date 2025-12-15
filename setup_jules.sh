#!/bin/bash
set -e

# Define variables
ANDROID_HOME="$HOME/android-sdk"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip"
CMDLINE_TOOLS_ZIP="commandlinetools.zip"
MARKER_FILE="$ANDROID_HOME/.setup_complete"

# Check for marker file to skip setup if already done
if [ -f "$MARKER_FILE" ]; then
    echo "Setup already completed (marker file found). Exiting."
    exit 0
fi

# Create directory structure
mkdir -p "$ANDROID_HOME/cmdline-tools"

# Download Command Line Tools if not already present
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "Downloading Android Command Line Tools..."
    curl -L "$CMDLINE_TOOLS_URL" -o "$CMDLINE_TOOLS_ZIP"

    echo "Extracting..."
    unzip -q "$CMDLINE_TOOLS_ZIP" -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm "$CMDLINE_TOOLS_ZIP"
else
    echo "Android Command Line Tools already installed."
fi

# Set environment variables for this script
export ANDROID_HOME="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# Persist environment variables to .bashrc for future sessions
if ! grep -q "ANDROID_HOME" ~/.bashrc; then
    echo "Configuring environment variables in .bashrc..."
    echo "export ANDROID_HOME=$ANDROID_HOME" >> ~/.bashrc
    echo "export PATH=\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH" >> ~/.bashrc
fi

# Accept licenses
echo "Accepting licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1 || true

# Install SDK packages
# API 35 (Android 15) as required by compileSdk 35
# Build Tools 34.0.0 (Latest stable as of typical defaults, or we could pick 35.0.0 if available)
echo "Installing Android SDK packages..."
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

# Create marker file to indicate successful completion
touch "$MARKER_FILE"

echo "Android SDK setup complete."
