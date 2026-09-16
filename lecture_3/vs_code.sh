#!/bin/bash

# tells you xdg... the fefault dekstop
DESKTOP_DIR="$(xdg-user-dir DESKTOP)"
SHORTCUT="$DESKTOP_DIR/Visual Studio Code.desktop"

cat > "$SHORTCUT" << 'EOF'
[Desktop Entry]
Version=1.0
Type=Application
Name=Visual Studio Code
Comment=Open Visual Studio Code
Exec=/usr/bin/code
Icon=/usr/share/pixmaps/vscode.png
Terminal=false
Categories=Development;IDE;
StartupNotify=true
EOF

chmod +x "$SHORTCUT"

#maybe run, not so sure
gio set "$SHORTCUT" metadata::trusted true 2>/dev/null || true

echo "VS Code shortcut done."
