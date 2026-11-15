class_name GhostCar
extends Node2D

## Silueta translúcida de la mejor vuelta. Se dibuja con el mismo código que el
## coche real, así que siempre coincide en forma y color.

var spec: Dictionary = {}


func setup(car_spec: Dictionary) -> void:
	spec = car_spec
	modulate = Color(1.0, 1.0, 1.0, 0.62)
	z_index = 9
	visible = false


func _draw() -> void:
	if not spec.is_empty():
		CarArt.draw_car(self, spec, 0.0, 0.0, 0.55)
