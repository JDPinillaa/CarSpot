# Simulador de Vehículos CarSpot

Repositorio del simulador Java/Swing. Incluye dos vehículos de referencia con
física longitudinal continua:
- **Volkswagen Golf GTI Mk8.5 europeo** (265 PS, DSG7, tracción delantera)
- **Subaru BRZ (ZD8) 2.4** (234 PS, manual de 6 marchas, tracción trasera)

Los datos publicados, estimaciones, fuentes y resultados de calibración están
separados en la documentación de cada vehículo.

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
- [Fuentes y modelo del Subaru BRZ (ZD8) 2.4 6MT](simuladorDeVehiculo/docs/referencia-subaru-brz-zd8.md)

## Estructura

```text
simuladorDeVehiculo/
├── data/          # Perfiles y parámetros de vehículos
├── docs/          # Fuentes, supuestos y calibración
├── src/           # Modelo, persistencia y aplicación Swing
├── nbproject/     # Configuración del proyecto Ant/NetBeans
└── README.md
```
