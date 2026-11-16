# MEMORY.md — Estado del proyecto

Memoria breve del simulador Java; actualizar cuando cambien decisiones o estado.

## Estado actual
- El repositorio se dedica únicamente a `simuladorDeVehiculo/` (Java 22/Swing).
- `autonoma.models` está modularizado por dominio:
  - `autonoma.models`: `Carro` y `PerfilVehiculo`.
  - `autonoma.models.motores`: `Motor`, `CurvaParMotor` y `MotorGolfGtiMk85`.
  - `autonoma.models.transmisiones`: `CajaCambios`, `AbstractCajaCambios`,
    `CajaDsg7GolfGti`, `CajaAutomatica` y `CajaManual`.
  - `autonoma.models.fisica`: `ResistenciaCarretera` y
    `ModeloAdherenciaLongitudinal`.
- Se eliminaron `autonoma.exceptions`, la mecánica heredada de accidente al
  apagar a >60 km/h y las clases `Llanta`/`LlantasBuenas` del ejercicio
  académico; `Carro` gestiona estados de forma continua y natural.
- `javac --release 22` compila sin errores; el ejecutable `ant` no está
  instalado en el entorno actual.
- El perfil inicial es Volkswagen Golf GTI Mk8.5 europeo de 265 PS, DQ381-7F,
  con integración longitudinal continua; documentación en
  `simuladorDeVehiculo/docs/referencia-golf-gti-mk8-5.md`.
- La curva de par representa 370 Nm entre 1.600–4.500 rpm y 195 kW entre
  5.250–6.500 rpm; zona de ralentí y extremos no publicados son estimaciones.
- La caja usa ratios del León 190 DQ381-7F y mapeo de finales provisional; no
  hay tabla VW confirmada para el GTI exacto. DQ381 cambia con mapa estimado.
- Resistencia EPA GTI EE. UU. 241 hp sirve de proxy; factor efectivo de par,
  adherencia, reparto y CG tienen estimaciones/calibración explícitas.
- AUTO BILD documenta unidad de prueba R19/235-35, 1.455 kg y reparto 64/36.
  Corrida Java a 0,01 s: perfil base 5,75/21,03 s y variante con masa/radio/
  reparto de esa prueba 5,65/20,87 s (0–100/200); pruebas publicadas 5,8/21,3.
  No recalibrar el factor global hasta verificar relaciones y resistencia.

## Decisiones permanentes
- Mantener modelo físico separado de Swing y audio; perfiles configurables en
  `data/especificaciones.txt`, modelos concretos en `models.motores` y
  `models.transmisiones`.
- No usar excepciones verificadas de negocio ni reglas discretas académicas para
  transiciones normales de conducción.
- Etiquetar datos como oficiales, derivados o estimados; nunca presentar una
  calibración como especificación de fábrica.
- Configuración localizable desde raíz, proyecto Ant o carpeta de clases; no
  depender únicamente de `user.dir`.
- `Cabina` se construye directamente en Java; no regenerarla desde formularios.

## Pendientes
- Confirmar relaciones y finales de la DQ381 del GTI Mk8.5 con documentación VW.
- Obtener resistencia de carretera de la variante europea y medición de vmax/
  aceleración en tramo alto para validar el límite de 250 km/h.
- Mejorar modelo de frenado con agarre por eje, ABS y carga dinámica.
