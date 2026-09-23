

#!/bin/bash
grep -qxF 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' ~/.bashrc || \
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64' >> ~/.bashrc

grep -qxF 'export PATH=$JAVA_HOME/bin:$PATH' ~/.bashrc || \
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

source ~/.bashrc

java -version
