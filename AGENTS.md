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
- `models.Carro` y `models.PerfilVehiculo`: estado del vehículo, integración
  longitudinal y ensamble del perfil; no dependen de Swing ni de audio.
- `models.fisica`: `ResistenciaCarretera` y `ModeloAdherenciaLongitudinal`
  (cálculos físicos encapsulados y reutilizables).
- `models.motores`: `Motor`, `CurvaParMotor` y motores concretos por vehículo.
- `models.transmisiones`: `CajaCambios`, `AbstractCajaCambios` y transmisiones
  concretas.
- `persistence.PersistenciaVehiculos`: carga y valida `data/especificaciones.txt`.
- `views`: cabina, tablero e indicadores Swing.
- `sounds`: efectos de audio opcionales.
- `docs/referencia-golf-gti-mk8-5.md`: fuentes, supuestos, calibración y pendientes.

## Convenciones
- Interfaz, nombres nuevos y comentarios en español; código Java tipado y
  responsabilidades de física y presentación separadas.
- Los perfiles viven en `data/especificaciones.txt`; las curvas específicas de
  motor y cajas se implementan en modelos dedicados.
- Marca cada dato como oficial, derivado o estimado/calibrado. No presentes
  calibraciones ni datos de otro vehículo como especificaciones de fábrica.
- Mantén README y MEMORY alineados con el contenido Java existente.
- No borres archivos ni modifiques comportamiento no relacionado con la tarea.

## Memoria
- Lee `MEMORY.md` al empezar y actualízalo al terminar cuando cambie el estado
  o una decisión. Máximo aproximado de 50 líneas.
- Si una decisión es una regla permanente, incorpórala aquí y resume la memoria.
- No guardes datos sensibles.
