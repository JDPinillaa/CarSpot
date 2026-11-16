# AGENTS.md — Guía del simulador

## Proyecto
Este repositorio se dedica al simulador de vehículos Java/Swing ubicado en
`simuladorDeVehiculo/`. Los vehículos de referencia actuales son el Volkswagen
Golf GTI Mk8.5 europeo (265 PS, DSG7, FWD) y el Subaru BRZ ZD8 2.4 (234 PS,
6MT, RWD). Prioriza datos trazables y un modelo extensible; evita incorporar
proyectos ajenos al simulador.

## Stack y ejecución
- Java 22, Swing y Ant/NetBeans; no se requieren dependencias externas.
- Compilar desde la raíz:
  `mkdir -p /tmp/carspot-java-build && find simuladorDeVehiculo/src -name '*.java' -print0 | xargs -0 javac --release 22 -d /tmp/carspot-java-build`
- Ejecutar desde la raíz:
  `java -cp /tmp/carspot-java-build autonoma.main.Main`
- También se puede abrir `simuladorDeVehiculo/` como proyecto Ant en NetBeans.

## Arquitectura
- `models.Carro`: clase abstracta con el estado del vehículo y la integración
  longitudinal SI; no depende de Swing ni de audio.
- `models.vehiculos`: subclases concretas de `Carro` (`GolfGtiMk85`,
  `SubaruBrzZd8`) y `CatalogoVehiculos`.
- `models.fisica`: `ResistenciaCarretera`, `ModeloAdherenciaLongitudinal` y
  `TipoTraccion` (cálculos físicos encapsulados y reutilizables).
- `models.motores`: `Motor`, `CurvaParMotor` y motores concretos por vehículo.
- `models.transmisiones`: `CajaCambios`, `AbstractCajaCambios` y transmisiones
  concretas.
- `views`: cabina, tablero e indicadores Swing.
- `sounds`: efectos de audio opcionales.
- `docs/`: fuentes, supuestos, calibración y pendientes de cada vehículo.

## Convenciones
- Interfaz, nombres nuevos y comentarios en español; código Java tipado y
  responsabilidades de física y presentación separadas.
- Cada vehículo se implementa como subclase de `Carro` en `models.vehiculos`,
  con sus curvas específicas en `models.motores` y `models.transmisiones`.
- Marca cada dato como oficial, derivado o estimado/calibrado. No presentes
  calibraciones ni datos de otro vehículo como especificaciones de fábrica.
- Mantén README y MEMORY alineados con el contenido Java existente.
- No borres archivos ni modifiques comportamiento no relacionado con la tarea.

## Memoria
- Lee `MEMORY.md` al empezar y actualízalo al terminar cuando cambie el estado
  o una decisión. Máximo aproximado de 50 líneas.
- Si una decisión es una regla permanente, incorpórala aquí y resume la memoria.
- No guardes datos sensibles.
