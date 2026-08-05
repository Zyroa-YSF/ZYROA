extends Node

# GameManager: controls overall game flow and target gameplay duration
# Autoload this script as 'GameManager' (project.godot updated accordingly)

@export var total_gameplay_seconds := 2400 # 40 minutes
var remaining_seconds := total_gameplay_seconds
var running := false

@onready var hud := null

func _ready():
    # Attempt to find HUD if present
    if has_node("/root/HUD"):
        hud = get_node("/root/HUD")

func start_game():
    running = true
    remaining_seconds = total_gameplay_seconds
    # Load main level scene if not already
    if get_tree().current_scene and get_tree().current_scene.filename != "res://scenes/main.tscn":
        get_tree().change_scene_to_file("res://scenes/main.tscn")

func end_game(reason := "time_elapsed"):
    running = false
    # For prototype, go to apartment interior final scene
    get_tree().change_scene_to_file("res://scenes/apartment_interior.tscn")

func _process(delta):
    if running:
        remaining_seconds = max(0, remaining_seconds - delta)
        # Update HUD if available
        if hud and hud.has_method("update_timer"):
            hud.update_timer(remaining_seconds)
        if remaining_seconds <= 0:
            end_game("time_up")
