# MEMORY.md — Diario de Estudio

Memoria del proyecto entre sesiones. Máximo ~50 líneas: resume o elimina lo que ya no aporte.

## Estado actual
- Refactor Godot 4.7 en `game2d/` jugable: 3 coches × 3 circuitos, física propia, HUD, menú, sectores, fantasma, derrapes y audio sintetizado.
- Verificado el 2026-11-15 con pruebas headless en las 9 combinaciones; vuelta de referencia: 33,450 s en Circuito Norte.
- `game2d/` aún no está commiteado; el `README.md` raíz apunta a `game/` y `game2d/docs/` está vacío.

## Decisiones (y por qué)
- Física custom en vez de cuerpos físicos de Godot: comportamiento ajustable por datos y control total.
- Coches y pistas como diccionarios en `car_specs.gd` y `track_data.gd`: añadir contenido sin tocar la simulación.
- Todo el arte, HUD, pista y audio se genera por código: cero dependencias y prototipos rápidos.
- El piloto automático limita a 16,8 m/s; es una decisión de los tests, no de la física del coche.

## Aprendizajes y errores a evitar
- El HUD espera la clave `lap`, pero `main.gd` envía `laps`: el contador siempre muestra “VUELTA 1”.
- No reiniciar `_lap_progress` a 0 a mitad de vuelta: usar `track.distance_at(idx)` para no desalinear meta y sectores.
- `EngineAudio` no se detiene al volver al menú ni al pausar; el motor sigue sonando.
- `on_track` usa `surface_grip > 0.9`, así que los pianos (0,82) cuentan como fuera de pista.
- El tope del autopiloto oculta la alta velocidad; las máximas estimadas son 272/265/249 km/h.

## Próximos pasos
- Commitear `game2d/`, corregir `README.md`/`.gitignore` y escribir la documentación en `game2d/docs/`.
- Corregir contador de vueltas, reinicio, audio en menú/pausa y criterio de piano.
- Elevar el límite del autopiloto y añadir una prueba de alta velocidad.
- Dividir `main.gd` en sesión, cronometraje, cámara, autopiloto y herramientas de desarrollo.
