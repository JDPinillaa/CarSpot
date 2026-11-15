class_name SkidMarks
extends Node2D

## Marcas de derrape persistentes. No son partículas: se guardan segmentos y se
## dibujan de una vez, así que las marcas se quedan en el asfalto toda la
## carrera sin coste de simulación.

const MAX_SEGMENTS := 1600

var _segments: Array = []


func _ready() -> void:
	z_index = 6


func add_segment(from: Vector2, to: Vector2, intensity: float) -> void:
	_segments.append({"a": from, "b": to, "i": clampf(intensity, 0.0, 1.0)})
	while _segments.size() > MAX_SEGMENTS:
		_segments.pop_front()
	queue_redraw()


func clear() -> void:
	_segments.clear()
	queue_redraw()


func count() -> int:
	return _segments.size()


func _draw() -> void:
	for segment in _segments:
		var alpha: float = 0.10 + 0.30 * float(segment["i"])
		draw_line(segment["a"], segment["b"], Color(0.03, 0.03, 0.04, alpha), 0.34, true)
