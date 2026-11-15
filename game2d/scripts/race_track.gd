class_name RaceTrack
extends Node2D

## Circuito procedural. Convierte una lista de puntos de control en una
## polilínea uniforme (Catmull-Rom), calcula tangentes, normales y curvatura,
## y construye toda la imagen de la pista por código:
##
##   - asfalto (Line2D gruesa con uniones redondeadas)
##   - pianos a cuadros (malla con colores por vértice)
##   - líneas de borde y línea de meta a cuadros
##   - césped, árboles, rocas, neumáticos y gradas
##
## La física consulta `nearest_index`, `lateral_at` y `surface_grip`.

const SAMPLE_SPACING := 2.0


var display_name := ""
var subtitle := ""
var width := 10.0
var points := PackedVector2Array()
var tangents := PackedVector2Array()
var normals := PackedVector2Array()
var curvatures := PackedFloat32Array()
var distances := PackedFloat32Array()
var total_length := 0.0
var theme: Dictionary = {}
var bounds := Rect2()

var _scenery: Array = []
var _patches: Array = []
var _stand_spots: Array = []
var _raceline: MeshInstance2D


func build(data: Dictionary) -> void:
	display_name = data["name"]
	subtitle = data.get("subtitle", "")
	width = float(data["width"])
	theme = data["theme"]

	var dense := _sample_catmull(data["points"], 24)
	_resample(dense, SAMPLE_SPACING)
	_compute_frames()
	_compute_bounds()
	_build_visuals()
	_build_scenery()
	queue_redraw()


# --- Muestreo ---------------------------------------------------------------

func _sample_catmull(control: Array, subdivisions: int) -> PackedVector2Array:
	var out := PackedVector2Array()
	var n := control.size()
	for i in n:
		var p0: Vector2 = control[(i - 1 + n) % n]
		var p1: Vector2 = control[i]
		var p2: Vector2 = control[(i + 1) % n]
		var p3: Vector2 = control[(i + 2) % n]
		for j in subdivisions:
			var t := float(j) / float(subdivisions)
			var t2 := t * t
			var t3 := t2 * t
			out.append(0.5 * (
				2.0 * p1
				+ (-p0 + p2) * t
				+ (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2
				+ (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3))
	out.append(out[0])
	return out


func _resample(dense: PackedVector2Array, spacing: float) -> void:
	var out := PackedVector2Array()
	out.append(dense[0])
	var travelled := 0.0
	var next_emit := spacing
	for i in range(1, dense.size()):
		var a := dense[i - 1]
		var b := dense[i]
		var seg := a.distance_to(b)
		if seg < 0.0001:
			continue
		while travelled + seg >= next_emit:
			var t := (next_emit - travelled) / seg
			out.append(a.lerp(b, t))
			next_emit += spacing
		travelled += seg
	points = out


func _compute_frames() -> void:
	tangents.clear()
	normals.clear()
	curvatures.clear()
	distances.clear()
	var n := points.size()
	var total := 0.0
	for i in n:
		var d := points[i].distance_to(points[(i + 1) % n])
		distances.append(total)
		total += d
	total_length = total

	for i in n:
		var prev := points[(i - 1 + n) % n]
		var next := points[(i + 1) % n]
		var t := (next - prev).normalized()
		tangents.append(t)
		# Normal a la derecha del sentido de marcha (en pantalla, +y).
		normals.append(Vector2(-t.y, t.x))

	for i in n:
		var a := tangents[(i + 1) % n].angle() - tangents[i].angle()
		var delta := wrapf(a, -PI, PI)
		curvatures.append(delta / SAMPLE_SPACING)


func _compute_bounds() -> void:
	var min_p := points[0]
	var max_p := points[0]
	for p in points:
		min_p = min_p.min(p)
		max_p = max_p.max(p)
	bounds = Rect2(min_p, max_p - min_p).grow(width)


# --- Consultas de física ----------------------------------------------------

func nearest_index(pos: Vector2, hint: int = -1) -> int:
	var n := points.size()
	var best := 0
	var best_d := INF
	if hint >= 0:
		for k in range(-40, 41):
			var idx := posmod(hint + k, n)
			var d := points[idx].distance_squared_to(pos)
			if d < best_d:
				best_d = d
				best = idx
		if best_d < 6400.0:  # 80 m: el hint sigue siendo válido
			return best

	best = 0
	best_d = INF
	for i in n:
		var d := points[i].distance_squared_to(pos)
		if d < best_d:
			best_d = d
			best = i
	return best


func lateral_at(pos: Vector2, index: int) -> float:
	return (pos - points[index]).dot(normals[index])


## 1.0 en asfalto, algo menos sobre el piano, 0.52 en césped.
func surface_grip(pos: Vector2, index: int) -> float:
	var lat := absf(lateral_at(pos, index))
	if lat <= width * 0.5:
		return 1.0
	if lat <= width * 0.5 + 1.1:
		return 0.82
	return 0.52


func on_track(pos: Vector2, index: int) -> bool:
	return absf(lateral_at(pos, index)) <= width * 0.5 + 1.0


func distance_at(index: int) -> float:
	return distances[posmod(index, distances.size())]


func sample_at_distance(d: float) -> Vector2:
	var wrapped := fposmod(d, total_length)
	var i := _index_at_distance(wrapped)
	var d0 := distances[i]
	var d1 := distances[i + 1] if i + 1 < distances.size() else total_length
	var t := (wrapped - d0) / maxf(0.001, d1 - d0)
	return points[i].lerp(points[(i + 1) % points.size()], t)


func tangent_at_distance(d: float) -> Vector2:
	var wrapped := fposmod(d, total_length)
	var i := _index_at_distance(wrapped)
	var d0 := distances[i]
	var d1 := distances[i + 1] if i + 1 < distances.size() else total_length
	var t := (wrapped - d0) / maxf(0.001, d1 - d0)
	return tangents[i].lerp(tangents[(i + 1) % tangents.size()], t).normalized()


func _index_at_distance(d: float) -> int:
	var lo := 0
	var hi := distances.size() - 1
	while lo < hi:
		var mid := (lo + hi + 1) / 2
		if distances[mid] <= d:
			lo = mid
		else:
			hi = mid - 1
	return lo


func start_position() -> Vector2:
	return points[0]


func start_heading() -> float:
	return tangents[0].angle()


# --- Construcción visual ----------------------------------------------------

func _build_visuals() -> void:
	var asphalt := Line2D.new()
	asphalt.name = "Asfalto"
	asphalt.points = points
	asphalt.width = width
	asphalt.default_color = theme["asphalt"]
	asphalt.joint_mode = Line2D.LINE_JOINT_ROUND
	asphalt.begin_cap_mode = Line2D.LINE_CAP_ROUND
	asphalt.end_cap_mode = Line2D.LINE_CAP_ROUND
	asphalt.closed = true
	asphalt.antialiased = true
	asphalt.z_index = 2
	add_child(asphalt)

	var shoulder := Line2D.new()
	shoulder.points = _offset_points(0.0)
	shoulder.width = width + 2.4
	shoulder.default_color = theme["asphalt_edge"]
	shoulder.joint_mode = Line2D.LINE_JOINT_ROUND
	shoulder.closed = true
	shoulder.z_index = 1
	add_child(shoulder)

	var curbs := _build_curbs()
	curbs.z_index = 3
	add_child(curbs)

	for side in [-1.0, 1.0]:
		var edge := Line2D.new()
		edge.points = _offset_points(side * (width * 0.5 - 0.35))
		edge.width = 0.18
		edge.default_color = theme["line"]
		edge.closed = true
		edge.z_index = 4
		add_child(edge)

	_raceline = _build_dashes(0.0, 0.26, Color(0.95, 0.95, 0.95, 0.34), 5.0, 8.0)
	_raceline.name = "Trazada"
	_raceline.z_index = 4
	_raceline.visible = false
	add_child(_raceline)

	_add_start_line()


func _offset_points(offset: float) -> PackedVector2Array:
	var out := PackedVector2Array()
	for i in points.size():
		out.append(points[i] + normals[i] * offset)
	return out


## Pianos a cuadros a ambos lados: una sola malla con colores por vértice.
func _build_curbs() -> MeshInstance2D:
	var verts := PackedVector2Array()
	var colors := PackedColorArray()
	var stripe := 3.2
	var s := 0.0
	var flip := false
	while s < total_length:
		var s2 := minf(s + stripe, total_length)
		var p0 := sample_at_distance(s)
		var p1 := sample_at_distance(s2)
		var t0 := tangent_at_distance(s)
		var t1 := tangent_at_distance(s2)
		var n0 := Vector2(-t0.y, t0.x)
		var n1 := Vector2(-t1.y, t1.x)
		var color: Color = theme["curb_a"] if flip else theme["curb_b"]
		for side_value in [-1.0, 1.0]:
			var side := float(side_value)
			var inner0: Vector2 = p0 + n0 * side * (width * 0.5 - 0.15)
			var outer0: Vector2 = p0 + n0 * side * (width * 0.5 + 1.05)
			var inner1: Vector2 = p1 + n1 * side * (width * 0.5 - 0.15)
			var outer1: Vector2 = p1 + n1 * side * (width * 0.5 + 1.05)
			verts.append_array([inner0, outer0, outer1, inner1])
			for k in 4:
				colors.append(color)
		flip = not flip
		s = s2
	return _mesh_instance(verts, colors)


## Guiones a lo largo de la pista, con desfase opcional de trazada.
func _build_dashes(offset: float, dash_width: float, color: Color, on_len: float, off_len: float) -> MeshInstance2D:
	var verts := PackedVector2Array()
	var colors := PackedColorArray()
	var s := 0.0
	while s < total_length:
		var s2 := minf(s + on_len, total_length)
		var p0 := sample_at_distance(s)
		var p1 := sample_at_distance(s2)
		var t0 := tangent_at_distance(s)
		var t1 := tangent_at_distance(s2)
		var n0 := Vector2(-t0.y, t0.x)
		var n1 := Vector2(-t1.y, t1.x)
		var o0 := offset
		var o1 := offset
		var a0 := p0 + n0 * (o0 + dash_width * 0.5)
		var b0 := p0 + n0 * (o0 - dash_width * 0.5)
		var a1 := p1 + n1 * (o1 + dash_width * 0.5)
		var b1 := p1 + n1 * (o1 - dash_width * 0.5)
		verts.append_array([a0, b0, b1, a1])
		for k in 4:
			colors.append(color)
		s += on_len + off_len
	return _mesh_instance(verts, colors)


func _mesh_instance(verts: PackedVector2Array, colors: PackedColorArray) -> MeshInstance2D:
	var indices := PackedInt32Array()
	for i in range(0, verts.size(), 4):
		indices.append_array([i, i + 1, i + 2, i, i + 2, i + 3])

	var arrays := []
	arrays.resize(Mesh.ARRAY_MAX)
	arrays[Mesh.ARRAY_VERTEX] = verts
	arrays[Mesh.ARRAY_COLOR] = colors
	arrays[Mesh.ARRAY_INDEX] = indices
	var mesh := ArrayMesh.new()
	mesh.add_surface_from_arrays(Mesh.PRIMITIVE_TRIANGLES, arrays)

	var instance := MeshInstance2D.new()
	instance.mesh = mesh
	instance.material = CanvasItemMaterial.new()
	return instance


func _add_start_line() -> void:
	var image := Image.create_empty(8, 2, false, Image.FORMAT_RGBA8)
	for x in 8:
		for y in 2:
			image.set_pixel(x, y, Color(0.94, 0.94, 0.92) if (x + y) % 2 == 0 else Color(0.10, 0.10, 0.12))
	var sprite := Sprite2D.new()
	sprite.name = "Meta"
	sprite.texture = ImageTexture.create_from_image(image)
	sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	sprite.position = points[0]
	sprite.rotation = tangents[0].angle()
	sprite.scale = Vector2(width / 8.0, 1.6)
	sprite.z_index = 5
	add_child(sprite)


# --- Decoración -------------------------------------------------------------

func _build_scenery() -> void:
	var rng := RandomNumberGenerator.new()
	rng.seed = 20261115

	var area := bounds.grow(120.0)

	for i in 190:
		var p := Vector2(
			rng.randf_range(area.position.x, area.end.x),
			rng.randf_range(area.position.y, area.end.y))
		if _distance_to_track(p) < width * 0.5 + 7.0:
			continue
		_patches.append({
			"pos": p,
			"r": rng.randf_range(4.0, 16.0),
			"color": theme["grass_alt"].lightened(rng.randf_range(-0.06, 0.10)),
		})

	var trees := 0
	var attempts := 0
	while trees < 230 and attempts < 1600:
		attempts += 1
		var p := Vector2(
			rng.randf_range(area.position.x, area.end.x),
			rng.randf_range(area.position.y, area.end.y))
		if _distance_to_track(p) < width * 0.5 + 8.0:
			continue
		var kind := "tree"
		if rng.randf() < 0.12:
			kind = "rock"
		_scenery.append({
			"pos": p,
			"kind": kind,
			"scale": rng.randf_range(0.8, 1.8),
			"tone": rng.randf_range(-0.08, 0.08),
			"angle": rng.randf() * TAU,
		})
		trees += 1

	# Pilas de neumáticos en algunas curvas.
	for i in 14:
		var idx := int(rng.randf() * points.size())
		var side := 1.0 if rng.randf() < 0.5 else -1.0
		var p := points[idx] + normals[idx] * side * (width * 0.5 + 3.2)
		_scenery.append({
			"pos": p,
			"kind": "tires",
			"scale": rng.randf_range(0.8, 1.3),
			"tone": 0.0,
			"angle": tangents[idx].angle(),
		})

	# Gradas en dos rectas.
	for frac in [0.10, 0.55]:
		var idx := int(points.size() * frac)
		var side := 1.0 if rng.randf() < 0.5 else -1.0
		var p := points[idx] + normals[idx] * side * (width * 0.5 + 13.0)
		_scenery.append({
			"pos": p,
			"kind": "stand",
			"scale": 1.0,
			"tone": 0.0,
			"angle": tangents[idx].angle(),
			"side": side,
		})


func toggle_raceline() -> void:
	if _raceline != null:
		_raceline.visible = not _raceline.visible


func _distance_to_track(p: Vector2) -> float:
	var best := INF
	for q in points:
		best = minf(best, p.distance_squared_to(q))
	return sqrt(best)


func _draw() -> void:
	draw_rect(bounds.grow(240.0), theme["grass"])
	for patch in _patches:
		draw_circle(patch["pos"], patch["r"], patch["color"])

	for item in _scenery:
		_draw_scenery(item)


func _draw_scenery(item: Dictionary) -> void:
	var p: Vector2 = item["pos"]
	var s: float = item["scale"]
	var tone: float = item["tone"]
	match item["kind"]:
		"tree":
			draw_circle(p + Vector2(2.0, 2.6) * s, 3.0 * s, Color(0.02, 0.04, 0.02, 0.35))
			draw_circle(p, 2.5 * s, Color(0.16 + tone, 0.30 + tone, 0.16 + tone))
			draw_circle(p + Vector2(-0.6, -0.8) * s, 1.6 * s, Color(0.22 + tone, 0.40 + tone, 0.20 + tone))
			draw_circle(p + Vector2(0.9, -1.5) * s, 0.9 * s, Color(0.30 + tone, 0.48 + tone, 0.24 + tone))
		"rock":
			draw_circle(p + Vector2(0.8, 1.0) * s, 2.1 * s, Color(0.04, 0.05, 0.04, 0.3))
			draw_circle(p, 2.0 * s, Color(0.42 + tone, 0.43 + tone, 0.40 + tone))
			draw_circle(p + Vector2(-0.5, -0.5) * s, 1.2 * s, Color(0.52 + tone, 0.53 + tone, 0.49 + tone))
		"tires":
			for k in 3:
				draw_circle(p + Vector2(cos(item["angle"]) * k * 0.85, sin(item["angle"]) * k * 0.85) * s,
					0.55 * s, Color(0.07, 0.07, 0.08))
				draw_circle(p + Vector2(cos(item["angle"]) * k * 0.85, sin(item["angle"]) * k * 0.85) * s,
					0.22 * s, Color(0.20, 0.20, 0.21))
		"stand":
			draw_set_transform(p, item["angle"], Vector2.ONE)
			var depth := 5.0
			var length := 16.0
			draw_rect(Rect2(-length * 0.5, -depth * 0.5, length, depth), Color(0.30, 0.31, 0.33))
			for row in 4:
				draw_rect(Rect2(-length * 0.5 + 0.4, -depth * 0.5 + 0.4 + row * 1.15, length - 0.8, 0.55),
					Color(0.62, 0.63, 0.66).darkened(row * 0.05))
			draw_rect(Rect2(-length * 0.5, -depth * 0.5 - 0.7, length, 0.7), Color(0.20, 0.42, 0.62))
			draw_set_transform(Vector2.ZERO, 0.0, Vector2.ONE)
