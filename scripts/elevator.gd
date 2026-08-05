extends Node2D

# Elevator interaction: on player interact change to tunnel scene

@onready var area := $Area

func _ready():
    area.connect("body_entered", Callable(self, "_on_body_entered"))

func _on_body_entered(body):
    if body.name == "Player":
        # Show prompt to interact (in-engine UI not implemented yet)
        # Wait for player input
        body.set_meta("can_interact", true)

# This function is called by player.interact() when near
func on_player_interact(player):
    # Play elevator animation / sound (placeholder)
    get_tree().change_scene_to_file("res://scenes/tunnel.tscn")
