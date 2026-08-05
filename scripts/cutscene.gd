extends Node2D

# Cutscene trigger: rotate camera and start NPC chase

@export var npc_scene := "res://scenes/npc.tscn" # placeholder

func _ready():
    $CutsceneTrigger.connect("body_entered", Callable(self, "_on_trigger"))

func _on_trigger(body):
    if body.name == "Player":
        start_cutscene(body)

func start_cutscene(player):
    # Camera rotates/backwards effect placeholder
    # Spawn NPC and start chase
    var npc = preload("res://scripts/npc.gd").new()
    add_child(npc)
    npc.global_position = player.global_position + Vector2(-200,0)
    npc.start_chase(player)
    # After a bit, change to market scene to simulate teleport
    yield(get_tree().create_timer(2.0), "timeout")
    get_tree().change_scene_to_file("res://scenes/market.tscn")
