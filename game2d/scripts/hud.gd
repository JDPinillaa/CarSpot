class_name Hud
extends Control

## Tablero dibujado a mano con `_draw()`. No usa escenas ni texturas: todo es
## tipografía, arcos y rectángulos, lo que permite ajustar el diseño con datos.

const BG := Color(0.040, 0.050, 0.065, 0.88)
const BG_SOFT := Color(0.055, 0.065, 0.085, 0.72)
const ACCENT := Color(0.98, 0.76, 0.22)
const GREEN := Color(0.40, 0.92, 0.48)
const RED := Color(0.96, 0.32, 0.28)
const CYAN := Color(0.42, 0.86, 0.98)

var state: Dictionary = {}

var _panel: StyleBoxFlat
var _panel_soft: StyleBoxFlat
var _mini_norm := PackedVector2Array()
var _world_min := Vector2.ZERO
var _world_span := 1.0
var _font: Font


func _ready() -> void:
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	_font = ThemeDB.fallback_font

	_panel = StyleBoxFlat.new()
	_panel.bg_color = BG
	_panel.set_corner_radius_all(10)
	_panel.set_border_width_all(1)
	_panel.border_color = Color(1, 1, 1, 0.07)

	_panel_soft = StyleBoxFlat.new()
	_panel_soft.bg_color = BG_SOFT
	_panel_soft.set_corner_radius_all(8)


func set_track(track: RaceTrack) -> void:
	_mini_norm.clear()
	var min_p := track.points[0]
	var max_p := track.points[0]
	for p in track.points:
		min_p = min_p.min(p)
		max_p = max_p.max(p)
	_world_min = min_p
	_world_span = maxf(1.0, maxf(max_p.x - min_p.x, max_p.y - min_p.y))
	for p in track.points:
		_mini_norm.append((p - min_p) / _world_span)


func _draw() -> void:
	if state.is_empty():
		return
	var view := get_viewport_rect().size
	_draw_timing(view)
	_draw_speed(view)
	_draw_minimap(view)
	_draw_info(view)
	_draw_center(view)
	_draw_message(view)
	if bool(state.get("paused", false)):
		_draw_pause(view)


# --- Cronometraje -----------------------------------------------------------

func _draw_timing(view: Vector2) -> void:
	var w := 372.0
	var x := (view.x - w) * 0.5
	var y := 12.0
	draw_style_box(_panel, Rect2(x, y, w, 110))

	draw_string(_font, Vector2(x + 18, y + 24), "VUELTA %d" % (int(state.get("laps", 0)) + 1),
		HORIZONTAL_ALIGNMENT_LEFT, -1, 13, Color(1, 1, 1, 0.55))
	_text_right(Vector2(x + w - 18, y + 24), "MEJOR  " + _fmt(float(state.get("best_lap", -1.0))),
		13, Color(ACCENT.r, ACCENT.g, ACCENT.b, 0.92))

	var valid: bool = bool(state.get("lap_valid", true))
	var lap_color := Color(1, 1, 1, 0.96) if valid else Color(1.0, 0.48, 0.38, 0.9)
	_text_center_at(x + w * 0.5, y + 68, _fmt(float(state.get("lap_time", 0.0))), 38, lap_color)

	var last_lap := float(state.get("last_lap", -1.0))
	draw_string(_font, Vector2(x + 18, y + 92), "ÚLTIMA " + _fmt(last_lap),
		HORIZONTAL_ALIGNMENT_LEFT, -1, 12, Color(1, 1, 1, 0.45))

	# Cajas de sector.
	var sector_times: Array = state.get("sectors", [])
	var sector_deltas: Array = state.get("sector_deltas", [])
	var box_w := 42.0
	var bx := x + w - 18.0 - box_w * 3.0 - 12.0
	for i in 3:
		var rect := Rect2(bx + i * (box_w + 6.0), y + 80, box_w, 18)
		var done: bool = i < sector_times.size()
		var color := Color(1, 1, 1, 0.08)
		if done:
			color = GREEN if float(sector_deltas[i] if i < sector_deltas.size() else 0.0) <= 0.0 else RED
			color.a = 0.28
		draw_rect(rect, color)
		var label := "--"
		if done:
			label = _sector_short(float(sector_times[i]))
		_text_center_at(rect.position.x + box_w * 0.5, rect.position.y + 13, label, 10, Color(1, 1, 1, 0.75))

	# Barra de progreso de la vuelta.
	var progress := clampf(float(state.get("progress", 0.0)), 0.0, 1.0)
	var bar := Rect2(x + 12, y + 102, w - 24, 3)
	draw_rect(bar, Color(1, 1, 1, 0.10))
	draw_rect(Rect2(bar.position, Vector2(bar.size.x * progress, bar.size.y)), ACCENT)
	for i in 3:
		var fx := bar.position.x + bar.size.x * float(i + 1) / 3.0
		draw_line(Vector2(fx, bar.position.y - 2), Vector2(fx, bar.position.y + 5), Color(1, 1, 1, 0.30), 1.0)


# --- Velocímetro ------------------------------------------------------------

func _draw_speed(view: Vector2) -> void:
	var center := Vector2(158.0, view.y - 152.0)
	var radius := 96.0
	var start := deg_to_rad(135.0)
	var sweep := deg_to_rad(270.0)

	draw_circle(center, radius + 20.0, Color(0.035, 0.045, 0.060, 0.86))
	draw_arc(center, radius, start, start + sweep, 64, Color(1, 1, 1, 0.10), 9, true)

	var max_speed := maxf(60.0, float(state.get("max_speed", 240.0)))
	var speed := float(state.get("speed_kmh", 0.0))
	var frac := clampf(speed / max_speed, 0.0, 1.0)

	# Franjas de color del arco.
	for i in 24:
		var f0 := float(i) / 24.0
		var f1 := float(i + 1) / 24.0
		if f1 > frac + 0.001:
			break
		var col := ACCENT.lerp(Color(1.0, 0.34, 0.22), clampf((f0 - 0.55) / 0.45, 0.0, 1.0))
		draw_arc(center, radius, start + sweep * f0, start + sweep * f1, 4, col, 9, true)

	for i in 11:
		var f := float(i) / 10.0
		var angle := start + sweep * f
		var inner := radius - (16.0 if i % 5 == 0 else 10.0)
		var outer := radius - 5.0
		var col := Color(1, 1, 1, 0.55 if i % 5 == 0 else 0.25)
		draw_line(center + Vector2.from_angle(angle) * inner, center + Vector2.from_angle(angle) * outer, col, 2.0, true)
		if i % 5 == 0:
			var label_pos := center + Vector2.from_angle(angle) * (radius - 34.0)
			_text_center_at(label_pos.x, label_pos.y + 4.0, str(int(max_speed * f)), 10, Color(1, 1, 1, 0.42))

	var needle_angle := start + sweep * frac
	var needle_from := center + Vector2.from_angle(needle_angle) * 16.0
	var needle_to := center + Vector2.from_angle(needle_angle) * (radius - 20.0)
	draw_line(needle_from, needle_to, ACCENT, 3.5, true)
	draw_circle(center, 8.0, Color(0.10, 0.11, 0.14))
	draw_circle(center, 3.0, ACCENT)

	_text_center_at(center.x, center.y + 40.0, "%d" % int(speed), 34, Color(1, 1, 1, 0.96))
	_text_center_at(center.x, center.y + 56.0, "km/h", 11, Color(1, 1, 1, 0.45))

	# Marcha.
	var gear := int(state.get("gear", 1))
	var gear_text := "N" if speed < 0.8 else str(gear)
	var gear_rect := Rect2(center.x + radius + 20.0, center.y - 34.0, 58.0, 68.0)
	draw_style_box(_panel_soft, gear_rect)
	_text_center_at(gear_rect.get_center().x, gear_rect.position.y + 44.0, gear_text, 36, ACCENT)
	_text_center_at(gear_rect.get_center().x, gear_rect.position.y + 60.0, "MARCHA", 9, Color(1, 1, 1, 0.40))

	# Barra de revoluciones y luz de cambio.
	var rpm_max := maxf(1000.0, float(state.get("rpm_max", 7500.0)))
	var rpm := float(state.get("rpm", 0.0))
	var bar_rect := Rect2(center.x - radius, center.y - radius - 34.0, radius * 2.0, 9.0)
	draw_rect(bar_rect, Color(1, 1, 1, 0.08))
	var rpm_frac := clampf(rpm / rpm_max, 0.0, 1.0)
	var rpm_col := GREEN.lerp(ACCENT, clampf(rpm_frac * 1.6, 0.0, 1.0)).lerp(RED, clampf((rpm_frac - 0.82) * 5.0, 0.0, 1.0))
	draw_rect(Rect2(bar_rect.position, Vector2(bar_rect.size.x * rpm_frac, bar_rect.size.y)), rpm_col)
	for i in range(1, 8):
		var tx := bar_rect.position.x + bar_rect.size.x * float(i) / 8.0
		draw_line(Vector2(tx, bar_rect.position.y), Vector2(tx, bar_rect.end.y), Color(0, 0, 0, 0.28), 1.0)

	var shift := float(state.get("shift", 0.0))
	var light_col := Color(0.10, 0.10, 0.12)
	if shift > 0.15 or rpm_frac > 0.94:
		light_col = RED.lerp(Color(1.0, 0.85, 0.35), 0.5 + 0.5 * sin(Time.get_ticks_msec() * 0.03))
	draw_circle(Vector2(bar_rect.end.x + 12.0, bar_rect.position.y + 4.5), 6.0, light_col)

	# Pedales.
	var throttle := clampf(float(state.get("throttle", 0.0)), 0.0, 1.0)
	var brake := clampf(float(state.get("brake", 0.0)), 0.0, 1.0)
	var pedal_x := center.x - 72.0
	draw_rect(Rect2(pedal_x, center.y + 68.0, 130.0, 5.0), Color(1, 1, 1, 0.08))
	draw_rect(Rect2(pedal_x, center.y + 68.0, 130.0 * throttle, 5.0), GREEN)
	draw_rect(Rect2(pedal_x, center.y + 78.0, 130.0, 5.0), Color(1, 1, 1, 0.08))
	draw_rect(Rect2(pedal_x, center.y + 78.0, 130.0 * brake, 5.0), RED)
	draw_string(_font, Vector2(pedal_x - 16.0, center.y + 74.0), "T", HORIZONTAL_ALIGNMENT_LEFT, -1, 10, Color(1, 1, 1, 0.35))
	draw_string(_font, Vector2(pedal_x - 16.0, center.y + 84.0), "F", HORIZONTAL_ALIGNMENT_LEFT, -1, 10, Color(1, 1, 1, 0.35))


# --- Minimapa ---------------------------------------------------------------

func _draw_minimap(view: Vector2) -> void:
	var w := 212.0
	var h := 176.0
	var x := view.x - w - 16.0
	var y := view.y - h - 16.0
	draw_style_box(_panel, Rect2(x, y, w, h))
	draw_string(_font, Vector2(x + 12, y + 18), "CIRCUITO", HORIZONTAL_ALIGNMENT_LEFT, -1, 10, Color(1, 1, 1, 0.40))

	var area := Rect2(x + 14.0, y + 26.0, w - 28.0, h - 40.0)
	var side := minf(area.size.x, area.size.y)
	var origin := area.position + (area.size - Vector2(side, side)) * 0.5

	if _mini_norm.size() > 1:
		var outline := PackedVector2Array()
		for norm in _mini_norm:
			outline.append(origin + norm * side)
		var closed := outline.duplicate()
		closed.append(outline[0])
		draw_polyline(closed, Color(0.02, 0.03, 0.04, 0.65), 5.0, true)
		draw_polyline(closed, Color(0.62, 0.68, 0.72, 0.55), 2.0, true)

		# Línea de meta.
		var a := outline[0]
		var b := outline[1]
		draw_line(a, b, Color(1, 1, 1, 0.85), 2.0)

		var ghost_pos: Variant = state.get("ghost_pos", null)
		if ghost_pos != null:
			var gp: Vector2 = (ghost_pos - _world_min) / _world_span
			draw_circle(origin + gp * side, 3.2, Color(CYAN.r, CYAN.g, CYAN.b, 0.8))

		var car_pos: Variant = state.get("car_pos", null)
		if car_pos != null:
			var cp: Vector2 = (car_pos - _world_min) / _world_span
			var screen := origin + cp * side
			var heading: float = float(state.get("car_heading", 0.0))
			draw_circle(screen, 4.2, ACCENT)
			draw_line(screen, screen + Vector2.from_angle(heading) * 8.0, Color(1, 1, 1, 0.9), 2.0, true)


# --- Información ------------------------------------------------------------

func _draw_info(view: Vector2) -> void:
	draw_style_box(_panel, Rect2(16, 16, 330, 76))
	draw_rect(Rect2(16, 16, 5, 76), state.get("car_color", ACCENT))
	draw_string(_font, Vector2(34, 42), String(state.get("track_name", "")),
		HORIZONTAL_ALIGNMENT_LEFT, -1, 21, Color(1, 1, 1, 0.94))
	draw_string(_font, Vector2(34, 63), String(state.get("car_name", "")),
		HORIZONTAL_ALIGNMENT_LEFT, -1, 13, Color(1, 1, 1, 0.60))
	draw_string(_font, Vector2(34, 82), "%d vueltas · %s" % [int(state.get("laps", 0)), _fmt(float(state.get("last_lap", -1.0)))],
		HORIZONTAL_ALIGNMENT_LEFT, -1, 11, Color(1, 1, 1, 0.38))

	_text_right(Vector2(view.x - 20, 32), "C cámara    R reinicio    G fantasma",
		12, Color(1, 1, 1, 0.44))
	_text_right(Vector2(view.x - 20, 50), "L trazada    M menú    ESC pausa",
		12, Color(1, 1, 1, 0.44))

	var aero := float(state.get("lateral_g", 0.0))
	var grip_use := clampf(absf(aero) / 1.35, 0.0, 1.0)
	if grip_use > 0.82:
		_text_right(Vector2(view.x - 20, 74), "LÍMITE DE AGARRE", 11, Color(1.0, 0.55, 0.28, 0.85))


func _draw_center(view: Vector2) -> void:
	var countdown := float(state.get("countdown", 0.0))
	if countdown > 0.0:
		var number := clampi(int(ceil(countdown)), 1, 3)
		var text := str(number)
		var pulse := 1.0 + 0.08 * sin(Time.get_ticks_msec() * 0.012)
		_text_center_at(view.x * 0.5, view.y * 0.44, text, int(140.0 * pulse), Color(1, 1, 1, 0.94))
		_text_center_at(view.x * 0.5, view.y * 0.52, "PREPARADO", 16, Color(ACCENT.r, ACCENT.g, ACCENT.b, 0.85))
		return

	var go_flash := float(state.get("go_flash", 0.0))
	if go_flash > 0.0:
		var alpha := clampf(go_flash, 0.0, 1.0)
		_text_center_at(view.x * 0.5, view.y * 0.44, "¡YA!", 96, Color(ACCENT.r, ACCENT.g, ACCENT.b, alpha))


func _draw_message(view: Vector2) -> void:
	var time := float(state.get("message_time", 0.0))
	if time <= 0.0:
		return
	var text := String(state.get("message", ""))
	if text.is_empty():
		return
	var alpha := clampf(time, 0.0, 1.0)
	var size := 22
	var width := _font.get_string_size(text, HORIZONTAL_ALIGNMENT_LEFT, -1, size).x
	var rect := Rect2(view.x * 0.5 - width * 0.5 - 18, view.y * 0.66 - 30, width + 36, 42)
	draw_style_box(_panel_soft, rect)
	draw_rect(Rect2(rect.position.x, rect.position.y, 4, rect.size.y), Color(ACCENT.r, ACCENT.g, ACCENT.b, alpha))
	_text_center_at(view.x * 0.5, rect.position.y + 27, text, size, Color(1, 1, 1, alpha))

	# Aviso de césped.
	if not bool(state.get("on_track", true)) and float(state.get("speed_kmh", 0.0)) > 25.0:
		var blink := 0.55 + 0.45 * sin(Time.get_ticks_msec() * 0.012)
		_text_center_at(view.x * 0.5, view.y * 0.60, "FUERA DE PISTA", 15, Color(1.0, 0.45, 0.30, blink))


func _draw_pause(view: Vector2) -> void:
	draw_rect(Rect2(Vector2.ZERO, view), Color(0.02, 0.03, 0.04, 0.62))
	_text_center_at(view.x * 0.5, view.y * 0.46, "PAUSA", 64, Color(1, 1, 1, 0.95))
	_text_center_at(view.x * 0.5, view.y * 0.54, "ESC para continuar  ·  M para volver al menú", 16, Color(1, 1, 1, 0.60))


# --- Utilidades -------------------------------------------------------------

func _text_center_at(cx: float, baseline: float, text: String, size: int, color: Color) -> void:
	var width := _font.get_string_size(text, HORIZONTAL_ALIGNMENT_LEFT, -1, size).x
	draw_string(_font, Vector2(cx - width * 0.5, baseline), text, HORIZONTAL_ALIGNMENT_LEFT, -1, size, color)


func _text_right(pos: Vector2, text: String, size: int, color: Color) -> void:
	var width := _font.get_string_size(text, HORIZONTAL_ALIGNMENT_LEFT, -1, size).x
	draw_string(_font, Vector2(pos.x - width, pos.y), text, HORIZONTAL_ALIGNMENT_LEFT, -1, size, color)


func _fmt(time: float) -> String:
	if time < 0.0:
		return "--:--.---"
	var minutes := int(time / 60.0)
	var seconds := time - minutes * 60.0
	return "%d:%06.3f" % [minutes, seconds]


func _sector_short(time: float) -> String:
	if time <= 0.0:
		return "--"
	return "%.1f" % time
