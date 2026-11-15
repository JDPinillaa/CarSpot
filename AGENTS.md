# AGENTS.md — Guía para agentes

## Proyecto
Simulador de Vehículo: refactor de un simulador académico Java/Swing a un juego de carreras top-down en Godot. El proyecto activo es `game2d/`; `simuladorDeVehiculo/` es legado de referencia y no se modifica salvo petición explícita.

## Stack
- Godot 4.7 + GDScript con tipado estático, render Forward+.
- Sin dependencias externas: física, arte, pistas, HUD y audio se generan por código.
- Física a 120 Hz con 4 subpasos; unidades SI (m, kg, N, rad, s; la UI muestra km/h).

## Ejecutar y probar
- Jugar: `godot --path game2d`
- Smoke: `godot --headless --path game2d -- --smoke`
- Vueltas: `godot --headless --path game2d -- --laps --time=45`
- Opciones: `--car=N --track=N`, `--shot`, `--menu_shot`.
- Ejecuta al menos el smoke headless antes y después de tocar física, cronometraje o flujo de juego.

## Arquitectura
- `main.gd`: estados MENU/COUNTDOWN/RACE/PAUSED, cronometraje por sectores, cámara, entrada, audio y pruebas.
- `car.gd` + `powertrain.gd`: modelo de bicicleta, neumáticos, motor de combustión y caja automática.
- `race_track.gd` + `track_data.gd`: circuito procedural Catmull-Rom y consultas de física.
- `car_specs.gd`: datos de coches, curvas de par y relaciones de caja.
- `hud.gd`, `menu.gd`, `car_art.gd`, `engine_audio.gd`, `skid_marks.gd`, `ghost_car.gd`: presentación.
- Los datos mandan: añade coches o pistas en los arrays de datos, sin duplicar lógica.

## Convenciones
- GDScript con tipos, nombres descriptivos y comentarios/UI en español.
- Mantén física y presentación separadas; no uses cuerpos físicos de Godot para el coche.
- Evita assets externos si el recurso puede generarse por código.
- Mantén el README sincronizado con la carpeta real (`game2d/`, no `game/`).
- No reescribas el legado Java ni borres archivos sin confirmar.

## Memoria
- Al empezar, lee `MEMORY.md` para conocer el estado del proyecto y las decisiones tomadas.
- Al terminar una tarea, actualízalo: estado actual, decisiones importantes (con su porqué) y errores a evitar.
- Mantenlo breve (máximo ~50 líneas): resume o elimina lo que ya no aporte.
- Si algo se convierte en una regla permanente, propón moverlo a `AGENTS.md` en lugar de dejarlo en la memoria.
- No guardes nunca datos sensibles (claves, tokens, datos personales).
