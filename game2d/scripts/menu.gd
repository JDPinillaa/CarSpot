class_name Menu
extends Control

## Menú de selección dibujado con `_draw()`. Navegación con teclado:
## A/D o flechas para el coche, W/S para el circuito, ENTER para arrancar.

signal start_requested(car_index: int, track_index: int)

const BG := Color(0.045, 0.055, 0.070)
const PANEL := Color(0.070, 0.085, 0.105, 0.96)
const PANEL_SEL := Color(0.105, 0.125, 0.150, 0.98)
const ACCENT := Color(0.98, 0.76, 0.22)
const TEXT := Color(0.94, 0.95, 0.96)
const MUTED := Color(0.58, 0.63, 0.68)

var active := true
var car_index := 0
var track_index := 0

var _font: Font
var _pulse := 0.0
var _mini_tracks: Array = []
var _styles: Dictionary = {}


func _ready() -> void:
	set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	_font = ThemeDB.fallback_font

	for selected in [false, true]:
		var style := StyleBoxFlat.new()
		style.bg_color = PANEL_SEL if selected else PANEL
		style.set_corner_radius_all(12)
		style.set_border_width_all(2 if selected else 1)
		style.border_color = ACCENT if selected else Color(1, 1, 1, 0.07)
		_styles[selected] = style

	for data in TrackData.TRACKS:
		_mini_tracks.append(_normalize_track(data["points"]))


func _process(delta: float) -> void:
	if not active:
		return
	_pulse += delta

	if Input.is_action_just_pressed("menu_next"):
		car_index = (car_index + 1) % CarSpecs.count()
	elif Input.is_action_just_pressed("menu_prev"):
		car_index = posmod(car_index - 1, CarSpecs.count())

	if Input.is_action_just_pressed("menu_down"):
		track_index = (track_index + 1) % TrackData.count()
	elif Input.is_action_just_pressed("menu_up"):
		track_index = posmod(track_index - 1, TrackData.count())

	if Input.is_action_just_pressed("start"):
		active = false
		start_requested.emit(car_index, track_index)
		return

	queue_redraw()


func _unhandled_input(event: InputEvent) -> void:
	if not active:
		return
	var key := event as InputEventKey
	if key != null and key.pressed and not key.echo and key.physical_keycode == KEY_ESCAPE:
		get_tree().quit()


func _draw() -> void:
	var view := get_viewport_rect().size
	draw_rect(Rect2(Vector2.ZERO, view), BG)

	# Rejilla de fondo y bandas de color.
	for x in range(0, int(view.x), 56):
		draw_line(Vector2(x, 0), Vector2(x, view.y), Color(1, 1, 1, 0.022), 1.0)
	for y in range(0, int(view.y), 56):
		draw_line(Vector2(0, y), Vector2(view.x, y), Color(1, 1, 1, 0.022), 1.0)
	draw_rect(Rect2(0, 0, view.x, 6), ACCENT)
	draw_circle(Vector2(view.x - 120, 120), 240, Color(ACCENT.r, ACCENT.g, ACCENT.b, 0.03))

	draw_string(_font, Vector2(64, 92), "SIMULADOR", HORIZONTAL_ALIGNMENT_LEFT, -1, 46, TEXT)
	draw_string(_font, Vector2(64, 136), "DE VEHÍCULO", HORIZONTAL_ALIGNMENT_LEFT, -1, 46, ACCENT)
	draw_string(_font, Vector2(66, 168), "Física propia · vista top-down · todo generado por código",
		HORIZONTAL_ALIGNMENT_LEFT, -1, 14, MUTED)

	draw_line(Vector2(604, 70), Vector2(604, view.y - 70), Color(1, 1, 1, 0.08), 1.0)

	draw_string(_font, Vector2(64, 216), "COCHE", HORIZONTAL_ALIGNMENT_LEFT, -1, 13, MUTED)
	draw_string(_font, Vector2(640, 216), "CIRCUITO", HORIZONTAL_ALIGNMENT_LEFT, -1, 13, MUTED)

	for i in CarSpecs.count():
		_draw_car_card(i, Rect2(64, 236 + i * 104, 500, 92), i == car_index)

	for i in TrackData.count():
		_draw_track_card(i, Rect2(640, 236 + i * 104, 576, 92), i == track_index)

	var spec := CarSpecs.get_car(car_index)
	var selected_rect := Rect2(64, 236, 500, 92)
	draw_string(_font, Vector2(selected_rect.position.x, 578), String(spec["blurb"]),
		HORIZONTAL_ALIGNMENT_LEFT, 500, 13, MUTED)

	var track := TrackData.get_track(track_index)
	draw_string(_font, Vector2(640, 578), "Ancho %d m · longitud aproximada %d m" % [
		int(track["width"]), int(_track_length(track["points"]))],
		HORIZONTAL_ALIGNMENT_LEFT, 576, 13, MUTED)

	draw_string(_font, Vector2(64, view.y - 38),
		"A/D coche    W/S circuito    ENTER arrancar    ESC salir",
		HORIZONTAL_ALIGNMENT_LEFT, -1, 13, MUTED)

	var button := Rect2(view.x - 316, view.y - 84, 252, 58)
	draw_rect(button, ACCENT)
	var pulse := 0.75 + 0.25 * sin(_pulse * 4.0)
	draw_rect(button.grow(4.0), Color(ACCENT.r, ACCENT.g, ACCENT.b, 0.25 * pulse), false, 2.0)
	var label := "ARRANCAR"
	var width := _font.get_string_size(label, HORIZONTAL_ALIGNMENT_LEFT, -1, 22).x
	draw_string(_font, Vector2(button.get_center().x - width * 0.5, button.position.y + 37),
		label, HORIZONTAL_ALIGNMENT_LEFT, -1, 22, Color(0.08, 0.09, 0.10))


func _draw_car_card(index: int, rect: Rect2, selected: bool) -> void:
	var spec := CarSpecs.get_car(index)
	draw_style_box(_styles[selected], rect)
	draw_rect(Rect2(rect.position.x + 10, rect.position.y + 12, 5, rect.size.y - 24), spec["color"])

	CarArt.draw_mini(self, spec, 9.0, Vector2(rect.position.x + 66, rect.position.y + 46))

	var text_color := TEXT if selected else Color(0.72, 0.76, 0.80)
	draw_string(_font, Vector2(rect.position.x + 120, rect.position.y + 33),
		String(spec["name"]), HORIZONTAL_ALIGNMENT_LEFT, -1, 22, text_color)
	draw_string(_font, Vector2(rect.position.x + 120, rect.position.y + 54),
		String(spec["subtitle"]), HORIZONTAL_ALIGNMENT_LEFT, -1, 12, MUTED)
	draw_string(_font, Vector2(rect.position.x + 120, rect.position.y + 76),
		"%d kg · %d CV · agarre %.2f g" % [
			int(spec["mass"]), CarSpecs.horsepower(spec),
			maxf(float(spec["grip_front"]), float(spec["grip_rear"]))],
		HORIZONTAL_ALIGNMENT_LEFT, -1, 11, Color(0.52, 0.57, 0.62))

	# Barra de relación peso/potencia.
	var ratio := clampf(1.0 - (CarSpecs.weight_power(spec) - 2.4) / 4.0, 0.0, 1.0)
	draw_rect(Rect2(rect.position.x + 340, rect.position.y + 68, 130, 5), Color(1, 1, 1, 0.08))
	draw_rect(Rect2(rect.position.x + 340, rect.position.y + 68, 130 * ratio, 5), spec["color"])


func _draw_track_card(index: int, rect: Rect2, selected: bool) -> void:
	var data := TrackData.get_track(index)
	draw_style_box(_styles[selected], rect)

	var area := Rect2(rect.position.x + 14, rect.position.y + 12, 92, rect.size.y - 24)
	var origin := area.position
	var side := minf(area.size.x, area.size.y)
	origin += (area.size - Vector2(side, side)) * 0.5

	var norm: PackedVector2Array = _mini_tracks[index]
	var outline := PackedVector2Array()
	for n in norm:
		outline.append(origin + n * side)
	var closed := outline.duplicate()
	closed.append(outline[0])
	draw_polyline(closed, Color(0.02, 0.03, 0.04, 0.7), 5.0, true)
	draw_polyline(closed, Color(0.66, 0.72, 0.76, 0.8 if selected else 0.45), 2.2, true)

	var text_color := TEXT if selected else Color(0.72, 0.76, 0.80)
	draw_string(_font, Vector2(rect.position.x + 126, rect.position.y + 34),
		String(data["name"]), HORIZONTAL_ALIGNMENT_LEFT, -1, 22, text_color)
	draw_string(_font, Vector2(rect.position.x + 126, rect.position.y + 56),
		String(data.get("subtitle", "")), HORIZONTAL_ALIGNMENT_LEFT, -1, 12, MUTED)

	var track_len := _track_length(data["points"])
	draw_string(_font, Vector2(rect.position.x + 126, rect.position.y + 77),
		"%d m de cuerda · %d vueltas de referencia" % [int(track_len), 3],
		HORIZONTAL_ALIGNMENT_LEFT, -1, 11, Color(0.52, 0.57, 0.62))

	var theme: Dictionary = data["theme"]
	draw_rect(Rect2(rect.end.x - 18, rect.position.y + 12, 5, rect.size.y - 24),
		theme["curb_a"])


func _normalize_track(points: Array) -> PackedVector2Array:
	var min_p: Vector2 = points[0]
	var max_p: Vector2 = points[0]
	for p in points:
		min_p = min_p.min(p)
		max_p = max_p.max(p)
	var span := maxf(1.0, maxf(max_p.x - min_p.x, max_p.y - min_p.y))
	var out := PackedVector2Array()
	for p in points:
		out.append((p - min_p) / span)
	return out


func _track_length(points: Array) -> float:
	var total := 0.0
	for i in points.size():
		total += points[i].distance_to(points[(i + 1) % points.size()])
	return total * 1.05
