#!/bin/bash

# Set variables
JDK_VERSION=17
JDK_URL=https://download.oracle.com/java/$JDK_VERSION/latest/jdk-${JDK_VERSION}_linux-x64_bin.tar.gz
INSTALL_DIR=$(pwd)/local_jdk
OCTANE_JAR=$(pwd)/lib/Octane.jar
SOURCE_FILE=$(pwd)/src/TagReportListenerImplementation.java
CLASS_FILE=$(pwd)/TagReportListenerImplementation.class
INTERFACES_JAR=$(pwd)/lib/interfaces.jar
TEMP_DIR=$(pwd)/temp

# Function to check if a command exists
command_exists () {
    command -v "$1" >/dev/null 2>&1 ;
}

# Create necessary directories
mkdir -p "$INSTALL_DIR"
mkdir -p "$TEMP_DIR"

# Check for existing local JDK installation
if [ -x "$INSTALL_DIR/bin/java" ]; then
    echo "Found existing JDK installation in $INSTALL_DIR."
else
    echo "No existing JDK installation found. Downloading and installing JDK $JDK_VERSION..."

    # Download the JDK
    cd "$TEMP_DIR"
    wget -O jdk.tar.gz $JDK_URL

    # Extract the JDK
    tar -xzf jdk.tar.gz -C "$INSTALL_DIR" --strip-components=1

    # Clean up the temporary directory
    rm jdk.tar.gz

    echo "JDK installed at $INSTALL_DIR."
fi

# Set the JAVA_HOME and PATH for the local JDK
export JAVA_HOME=$INSTALL_DIR
export PATH=$JAVA_HOME/bin:$PATH

# Compile the Java file using the Octane.jar
javac -cp "$OCTANE_JAR" "$SOURCE_FILE"

# Check if the compilation was successful
if [ -f "$CLASS_FILE" ]; then
    echo "Compilation successful. Moving the .class file to interfaces.jar..."

    # Update interfaces.jar with the new .class file
    jar uf "$INTERFACES_JAR" -C $(dirname "$CLASS_FILE") $(basename "$CLASS_FILE")

    echo "Update successful. Cleaning up..."

    # Optionally, delete the .class file after adding it to the jar
    rm "$CLASS_FILE"

    echo "Done."
else
    echo "Compilation failed. .class file not found."
fi

# Clean up the temporary directory
rm -rf "$TEMP_DIR"

echo "Local Java setup completed."
