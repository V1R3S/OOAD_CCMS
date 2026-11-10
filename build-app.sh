#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ -n "${JAVA_HOME:-}" ]; then
  PATH="$JAVA_HOME/bin:$PATH"
  export PATH
fi
if ! command -v javac >/dev/null 2>&1; then
  echo "Arena needs JDK 17 or newer. Set JAVA_HOME or add the JDK bin directory to PATH." >&2
  exit 1
fi
classpath_separator=':'
case "$(uname -s)" in
  CYGWIN*|MINGW*|MSYS*) classpath_separator=';' ;;
esac
# Fixed generated directories relative to the project root.
rm -rf build/app-classes build/check-classes
mkdir -p build/app-classes build/check-classes
find src/main/java -name '*.java' > build/sources.txt
javac -encoding UTF-8 --release 17 -d build/app-classes @build/sources.txt
jar --create --file build/arena.jar --main-class edu.aau.arena.ui.ArenaApp -C build/app-classes . -C . aau.png
if [ "${1:-}" = "test" ] || [ "${1:-}" = "ui-test" ]; then
  find src/test/java -name '*.java' ! -name 'AcceptanceMavenTest.java' > build/tests.txt
  javac -encoding UTF-8 --release 17 -cp build/app-classes -d build/check-classes @build/tests.txt
  java -Djava.awt.headless=true -cp "build/app-classes${classpath_separator}build/check-classes" edu.aau.arena.ArenaServiceTest
  java -Djava.awt.headless=true -cp "build/app-classes${classpath_separator}build/check-classes" edu.aau.arena.ArenaTournamentTest
  if [ "${1:-}" = "ui-test" ]; then
    java -cp "build/arena.jar${classpath_separator}build/check-classes" edu.aau.arena.ArenaUiSmokeTest
  fi
fi
