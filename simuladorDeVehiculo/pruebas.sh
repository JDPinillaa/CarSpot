#!/usr/bin/env sh
# Compila el simulador y ejecuta las pruebas deterministas del modelo físico.
set -eu

RAIZ=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
SALIDA="${TMPDIR:-/tmp}/carspot-java-build"

rm -rf "$SALIDA"
mkdir -p "$SALIDA"

find "$RAIZ/simuladorDeVehiculo/src" -name '*.java' -print0 \
    | xargs -0 javac --release 22 -d "$SALIDA"
find "$RAIZ/simuladorDeVehiculo/test" -name '*.java' -print0 \
    | xargs -0 javac --release 22 -cp "$SALIDA" -d "$SALIDA"

java -cp "$SALIDA" autonoma.tests.PruebasSimulador
