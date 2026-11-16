# MEMORY.md — Estado del proyecto

Memoria breve del simulador Java; actualizar cuando cambien decisiones o estado.

## Estado actual
- El repositorio se dedica únicamente a `simuladorDeVehiculo/` (Java 22/Swing).
- `autonoma.models` está modularizado por dominio:
  - `autonoma.models`: `Carro` (clase abstracta con la integración longitudinal,
    corte de inyección por marcha y protección al reducir).
  - `autonoma.models.vehiculos`: `GolfGtiMk85`, `SubaruBrzZd8` (subclases de
    `Carro`) y `CatalogoVehiculos`.
  - `autonoma.models.motores`: `Motor`, `CurvaParMotor`, `MotorGolfGtiMk85` y
    `MotorSubaruBrzZd8`.
  - `autonoma.models.transmisiones`: `CajaCambios`, `AbstractCajaCambios`,
    `CajaDsg7GolfGti`, `CajaAutomatica` y `CajaManual`.
  - `autonoma.models.fisica`: `ResistenciaCarretera`,
    `ModeloAdherenciaLongitudinal` y `TipoTraccion` (`DELANTERA`, `TRASERA`,
    `INTEGRAL`).
- Se eliminaron `especificaciones.txt`, `PersistenciaVehiculos`, `PerfilVehiculo`,
  `autonoma.exceptions`, `Llanta`/`LlantasBuenas` y el selector de interior.
- En vehículos manuales, `Cabina` permite subir y bajar marcha tanto con los
  botones en pantalla como con las teclas `+` y `-`.
- Dos vehículos activos en `CatalogoVehiculos`:
  1. **Volkswagen Golf GTI Mk8.5 europeo (`GolfGtiMk85`, 265 PS, DSG7, FWD)**:
     0–100 km/h en 5,75 s y 0–200 km/h en 21,03 s (`dt = 0,01 s`); documentado
     en `simuladorDeVehiculo/docs/referencia-golf-gti-mk8-5.md`.
  2. **Subaru BRZ ZD8 2.4 (`SubaruBrzZd8`, 234 PS, TL70 6MT, RWD)**:
     Relaciones 3,626/2,188/1,541/1,213/1,000/0,767 y final único 4,100
     oficiales, resistencia EPA 2025 BRZ 2.4L 6MT R18 (`Target Coef A/B/C`).
     0–100 km/h en 6,29 s (oficial 6,3 s), 0–160 km/h en 14,29 s, 0–200 km/h en
     24,98 s y frenado 100–0 en 33,58 m; documentado en
     `simuladorDeVehiculo/docs/referencia-subaru-brz-zd8.md`.

## Decisiones permanentes
- Mantener modelo físico separado de Swing y audio; cada vehículo extiende
  `Carro` en `models.vehiculos`.
- No usar excepciones verificadas de negocio ni reglas discretas académicas para
  transiciones normales de conducción.
- Etiquetar datos como oficiales, derivados o estimados; nunca presentar una
  calibración como especificación de fábrica.
- `Cabina` se construye directamente en Java; no regenerarla desde formularios.

## Pendientes
- Confirmar relaciones y finales de la DQ381 del GTI Mk8.5 con documentación VW.
- Obtener resistencia de carretera de la variante europea del GTI y medición de
  vmax/aceleración en tramo alto.
- Mejorar modelo de frenado con agarre por eje, ABS y carga dinámica.
