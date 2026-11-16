# Simulador de Vehículos CarSpot

Repositorio del simulador Java/Swing. El desarrollo actual toma como primer
vehículo de referencia el Volkswagen Golf GTI Mk8.5 europeo de 265 PS y DSG7.
Los datos publicados, estimaciones, fuentes y resultados de calibración están
separados en la documentación del vehículo.

## Requisitos

- JDK 22 o compatible.
- No requiere librerías externas.

## Compilar y ejecutar

Desde la raíz del repositorio:

```bash
mkdir -p /tmp/carspot-java-build
find simuladorDeVehiculo/src -name '*.java' -print0 | xargs -0 javac --release 22 -d /tmp/carspot-java-build
java -cp /tmp/carspot-java-build autonoma.main.Main
```

También puedes abrir [`simuladorDeVehiculo/`](simuladorDeVehiculo/) como
proyecto Ant en NetBeans. El simulador carga sus perfiles desde
`simuladorDeVehiculo/data/especificaciones.txt`.

## Documentación

- [Guía del simulador, perfiles y controles](simuladorDeVehiculo/README.md)
- [Fuentes y modelo del Volkswagen Golf GTI Mk8.5](simuladorDeVehiculo/docs/referencia-golf-gti-mk8-5.md)

## Estructura

```text
simuladorDeVehiculo/
├── data/          # Perfiles y parámetros de vehículos
├── docs/          # Fuentes, supuestos y calibración
├── src/           # Modelo, persistencia y aplicación Swing
├── nbproject/     # Configuración del proyecto Ant/NetBeans
└── README.md
```
