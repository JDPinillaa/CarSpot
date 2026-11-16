# MEMORY.md — Estado del proyecto

Memoria breve del simulador Java; actualizar cuando cambien decisiones o estado.

## Estado actual
- Repositorio dedicado a `simuladorDeVehiculo/` (Java 22/Swing). Versión **1.1.0**
  en la rama `feature/simulador-v1.1`; `CHANGELOG.md` resume los cambios.
- `autonoma.models` modularizado por dominio:
  - `autonoma.models`: `Carro` (clase abstracta, integración longitudinal,
    corte de inyección, protección al reducir y freno motor).
  - `autonoma.models.vehiculos`: `GolfGtiMk85`, `SubaruBrzZd8` y
    `CatalogoVehiculos`.
  - `autonoma.models.motores`: `Motor`, `CurvaParMotor` y motores concretos.
  - `autonoma.models.transmisiones`: `CajaCambios`, `AbstractCajaCambios`,
    `CajaDsg7GolfGti`, `CajaAutomatica` y `CajaManual`.
  - `autonoma.models.fisica`: `ResistenciaCarretera`,
    `ModeloAdherenciaLongitudinal` y `TipoTraccion`.
- Suite de pruebas deterministas sin dependencias en
  `simuladorDeVehiculo/test/autonoma/tests/PruebasSimulador.java`; se ejecuta
  con `./simuladorDeVehiculo/pruebas.sh` o `ant pruebas`. 15 comprobaciones.
- Vehículos activos:
  1. **Volkswagen Golf GTI Mk8.5 (265 PS, DSG7, FWD)**: 0–100 en 5,75 s,
     0–200 en 21,03 s, vmax 250 km/h; frenada 100–0 en 36,38 m.
  2. **Subaru BRZ ZD8 2.4 (234 PS, 6MT, RWD)**: 0–100 en ~6,3 s, 0–200 en
     ~25 s, vmax 226 km/h; frenada 100–0 en 33,7 m.
- Al apagar el motor en marcha se mantiene la marcha engranada y se aplica un
  par estimado de fricción/bombeo (18 % del par de la curva). Desde 100 km/h el
  coche se detiene en 36–38 s; documentado como supuesto, no dato de fábrica.
- `Cabina` libera los pedales al perder el foco y admite `W`/`S` además del
  mouse. Los manuales cambian con `+`/`-` o botones.

## Decisiones permanentes
- Mantener el modelo físico separado de Swing y audio; cada vehículo extiende
  `Carro` en `models.vehiculos`.
- No usar excepciones verificadas de negocio ni reglas discretas académicas.
- Etiquetar datos como oficiales, derivados o estimados; nunca presentar una
  calibración como especificación de fábrica.
- `Cabina` se construye directamente en Java; no regenerarla desde formularios.

## Pendientes
- Confirmar relaciones y finales de la DQ381 del GTI Mk8.5 con documentación VW.
- Obtener resistencia de carretera de la variante europea del GTI y medición de
  vmax/aceleración en tramo alto.
- Mejorar el frenado con agarre por eje, ABS y transferencia de carga.
- Modelar punto muerto/desembrague para el apagado en marcha y el planeo.
