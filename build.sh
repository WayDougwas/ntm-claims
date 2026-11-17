#!/bin/sh
# Builds ntm-claims-<version>.jar with plain javac (Java 21).
# CP must list the jars this mod compiles against (separated by ';' on Windows, ':' elsewhere):
#   NeoForge 1.21.1 (neoforge universal + client srg jar, fml loader, bus, sponge-mixin, brigadier,
#   datafixerupper, slf4j-api), NTM - Continuum (hbm-neoforge), Golem Core,
#   FTB Chunks, FTB Library, FTB Teams, Architectury.
# Example:  CP="$(cat cp.txt)" ./build.sh
set -e
VERSION=$(sed -n 's/^version = "\(.*\)"/\1/p' res/META-INF/neoforge.mods.toml)
mkdir -p out
javac --release 21 -proc:none -encoding UTF-8 -cp "$CP" -d out $(find src -name '*.java')
cp -r res/. out/
(cd out && jar --create --file ../ntm-claims-$VERSION.jar .)
echo "built ntm-claims-$VERSION.jar"
