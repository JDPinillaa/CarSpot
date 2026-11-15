# MEMORY.md — Diario de Estudio

Memoria del proyecto entre sesiones. Máximo ~50 líneas.

## Estado actual
- Visión de CarSpot: tres juegos; versión 1 Java/Swing existente para mejorar, versión 2 Godot 2D jugable y versión 3D futura aún sin tecnología ni alcance definidos.
- Juego Godot 4.7 en `game2d/`: 3 coches × 3 circuitos, física propia, HUD, menú, cronometraje, fantasma, derrapes y audio sintetizado.
- Rama `feature/game2d-v1`; cambios de `game2d/` subidos a `origin` en los commits `a0d9a17` y `b2c57e2`.
- Revisión 2026-11-15: smoke headless pasó; prueba de 45 s cerró una vuelta válida en 33,442 s, máxima 91,3 km/h y 0 s fuera de pista.
- README raíz presenta las tres etapas y enlaza las instrucciones de ejecución de ambas versiones existentes.

## Decisiones
- Física custom en vez de cuerpos físicos de Godot: comportamiento ajustable por datos y control total.
- Coches y pistas como diccionarios en `car_specs.gd` y `track_data.gd`: añadir contenido sin duplicar lógica.
- Arte, HUD, pista y audio generados por código: no requieren dependencias externas.

## Aprendizajes y mejoras pendientes
- El HUD usa `laps`; `main.gd` le entrega esa clave correctamente.
- Reiniciar el coche invalida la vuelta y coloca `_lap_progress` en `track.distance_at(idx)` para que el siguiente cruce de meta rearme el cronómetro.
- El audio se detiene al volver al menú y pausar, y se reanuda al iniciar/reanudar la carrera.
- `car.on_track` acepta grip >= 0.8 (incluye pianos a 0.82); `RaceTrack.on_track()` usa ancho + 1,0 m mientras `surface_grip()` considera piano hasta +1,1 m. Unificar el límite para evitar resultados distintos en consultas.
- El autopiloto fue conservador en el smoke de 45 s: 91,3 km/h máximos frente al límite de planificación de 45 m/s. Ajustar velocidades objetivo y revisar salidas de curva sin sacrificar estabilidad.
- El smoke por defecto cubre solo GT Ligero / Circuito Norte; ejecutar pruebas de combinaciones con `--car=N --track=N` al validar cambios generales.

## Próximos pasos
- Ajustar y medir el autopiloto en los 3 circuitos y coches.
- Unificar el borde de pista entre `RaceCar` y `RaceTrack`.
- Mejorar la versión Java y definir más adelante el alcance de la versión 3D.
