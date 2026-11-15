class_name EngineAudio
extends AudioStreamPlayer

## Motor de audio sintetizado en tiempo real.
##
## No usa archivos: genera la onda con un generador de flujo y ata la
## frecuencia al régimen del motor. Sube el tono al acelerar, ruge en el
## limitador y añade chillido de neumático cuando el coche desliza.

const MIX_RATE := 22050.0
const CYLINDER_FIRINGS := 2.0  # 4 cilindros, 4 tiempos: 2 explosiones por vuelta

var _playback: AudioStreamGeneratorPlayback
var _phase := 0.0
var _rpm := Powertrain.IDLE_RPM
var _throttle := 0.0
var _slip := 0.0
var _speed := 0.0
var _noise_lp := 0.0
var _amp_smooth := 0.1


func _ready() -> void:
	var generator := AudioStreamGenerator.new()
	generator.mix_rate = MIX_RATE
	generator.buffer_length = 0.08
	stream = generator
	volume_db = -10.0
	start_engine()


## Reanuda el generador. Es idempotente: se puede llamar al arrancar una
## carrera o al salir de una pausa sin reiniciar el audio dos veces.
func start_engine() -> void:
	if stream == null:
		return
	if not playing:
		play()
	_playback = get_stream_playback() as AudioStreamGeneratorPlayback


## Detiene el generador para que el motor no siga sonando en el menú o en pausa.
func stop_engine() -> void:
	stop()
	_playback = null


## Estado que llega desde el vehículo cada frame.
func set_state(rpm: float, throttle: float, slip: float, speed_kmh: float) -> void:
	_rpm = rpm
	_throttle = throttle
	_slip = clampf(slip * 3.0, 0.0, 1.0)
	_speed = speed_kmh


func _process(_delta: float) -> void:
	if _playback == null:
		return
	var frames := _playback.get_frames_available()
	if frames <= 0:
		return

	var frequency := _rpm / 60.0 * CYLINDER_FIRINGS
	var target_amp := 0.10 + 0.30 * _throttle + 0.05 * clampf(_rpm / Powertrain.LIMITER_RPM, 0.0, 1.0)
	_amp_smooth = lerpf(_amp_smooth, target_amp, 0.05)

	var buffer := PackedVector2Array()
	buffer.resize(frames)
	var phase := _phase
	var noise_lp := _noise_lp
	var step := frequency / MIX_RATE

	for i in frames:
		phase = fmod(phase + step, 1.0)
		var sample := sin(TAU * phase) * 0.60
		sample += sin(TAU * phase * 2.0) * 0.22
		sample += sin(TAU * phase * 3.0) * 0.10
		sample += sin(TAU * phase * 0.5) * 0.16
		sample *= _amp_smooth

		# Ruido filtrado: siseo de admisión y chillido de neumático.
		noise_lp = lerpf(noise_lp, randf() * 2.0 - 1.0, 0.28)
		sample += noise_lp * (0.02 + 0.10 * _slip + 0.012 * clampf(_speed / 200.0, 0.0, 1.0))

		sample = clampf(sample, -1.0, 1.0)
		buffer[i] = Vector2(sample, sample)

	_phase = phase
	_noise_lp = noise_lp
	_playback.push_buffer(buffer)
