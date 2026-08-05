extends Node2D

# Tunnel logic: flashlight, battery, ambient sounds. If player reaches end, go to corridor.

@export var battery_life := 100
@export var flashlight_node_path := "Light2D"

@onready var light := $Light2D

func _ready():
    light.visible = false

func toggle_flashlight():
    light.visible = not light.visible

func _process(delta):
    if light.visible:
        battery_life = max(0, battery_life - delta * 2)
    # Placeholder: detect when player reaches tunnel end using an Area2D in scene (not implemented in text scene)

func reach_tunnel_end():
    # Called by Area2D when player reaches the exit
    get_tree().change_scene_to_file("res://scenes/corridor.tscn")
