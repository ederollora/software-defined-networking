#!/bin/bash

set -e

IDEA_FOLDER="idea-IU-262.10968.63"
IDEA_SOURCE="$HOME/Downloads/$IDEA_FOLDER"
IDEA_BASE="/opt/idea"
IDEA_DEST="$IDEA_BASE/$IDEA_FOLDER"

DESKTOP="$HOME/Desktop"
DESKTOP_FILE="$DESKTOP/intellij-idea.desktop"

echo "=== Installing IntelliJ IDEA ==="

# Create destination folder
sudo mkdir -p "$IDEA_BASE"

# Move IntelliJ from Downloads to /opt/idea
if [ -d "$IDEA_SOURCE" ]; then
    echo "Moving IntelliJ from Downloads..."
    sudo mv "$IDEA_SOURCE" "$IDEA_BASE/"
elif [ -d "$IDEA_DEST" ]; then
    echo "IntelliJ is already installed in:"
    echo "$IDEA_DEST"
else
    echo "ERROR: IntelliJ folder not found."
    echo "Expected:"
    echo "$IDEA_SOURCE"
    exit 1
fi

# Make IntelliJ executable
sudo chmod +x "$IDEA_DEST/bin/idea.sh"

# Create Desktop if necessary
mkdir -p "$DESKTOP"

# Create Desktop shortcut
cat > "$DESKTOP_FILE" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=IntelliJ IDEA
Comment=IntelliJ IDEA
Exec=$IDEA_DEST/bin/idea.sh
Icon=$IDEA_DEST/bin/idea.svg
Terminal=false
Categories=Development;IDE;
EOF

# Make shortcut executable
chmod +x "$DESKTOP_FILE"

# Mark shortcut as trusted
gio set "$DESKTOP_FILE" metadata::trusted true 2>/dev/null || true

echo
echo "======================================"
echo "IntelliJ IDEA installed successfully"
echo
echo "Installed in:"
echo "$IDEA_DEST"
echo
echo "Desktop shortcut:"
echo "$DESKTOP_FILE"
echo "======================================"
