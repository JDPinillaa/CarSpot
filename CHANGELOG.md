# Historial de cambios

## 1.1.0 — 2026-11-15

### Añadido
- Suite de pruebas deterministas del modelo longitudinal en
  `simuladorDeVehiculo/test/autonoma/tests/PruebasSimulador.java`, sin
  dependencias externas: aceleración, velocidad máxima, frenada, protección de
  sobrerrevoluciones, rango de marchas de la DSG, resistencia y apagado.
- Script `simuladorDeVehiculo/pruebas.sh` y objetivo `ant pruebas`.
- Controles de teclado **`W`** (acelerador) y **`S`** (freno).

### Corregido
- Los pedales se liberan al perder el foco de la ventana, evitando que queden
  aplicados al cambiar de aplicación o abrir un diálogo.
- Al apagar el motor en marcha, la transmisión sigue conectada y se aplica un
  par estimado de fricción y bombeo. Desde 100 km/h el vehículo se detiene en
  unos 36–38 s, no en varios minutos.
- La resistencia de rodadura deja de anularse por debajo de 0,05 m/s y la
  simulación se detiene limpiamente a baja velocidad.
- La documentación del Golf corrige el tiempo real de 3.ª a 7.ª al levantar el
  acelerador: unos 0,5 s, no 1,5 s.

### Documentación
- README raíz y del módulo actualizados con versión, pruebas, controles y
  comportamiento del motor apagado.
- Supuestos de apagado en marcha documentados en las referencias del Golf GTI
  Mk8.5 y del Subaru BRZ ZD8.

## 1.0.0

- Simulador longitudinal Java 22/Swing con Golf GTI Mk8.5 (DSG7, FWD) y
  Subaru BRZ ZD8 (6MT, RWD), selector de garaje, tablero en tiempo real,
  curvas de par, transmisiones, resistencias y documentación de fuentes.
