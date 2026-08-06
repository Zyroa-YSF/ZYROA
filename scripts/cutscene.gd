extends Node2D

# Cutscene trigger: rotate camera and start NPC chase

@export var npc_scene_path := "res://scenes/npc.tscn" # use NPC scene for instancing

func _ready():
    if has_node("CutsceneTrigger"):
        $CutsceneTrigger.connect("body_entered", Callable(self, "_on_trigger"))

func _on_trigger(body):
    if body and body.name == "Player":
        start_cutscene(body)

func start_cutscene(player):
    # Camera rotates/backwards effect placeholder
    # Spawn NPC and start chase
    var npc_scene = preload(npc_scene_path)
    var npc = npc_scene.instantiate()
    add_child(npc)
    npc.global_position = player.global_position + Vector2(-200, 0)
    if npc.has_method("start_chase"):
        npc.start_chase(player)
    # After a bit, change to market scene to simulate transition
    await get_tree().create_timer(2.0).timeout
    get_tree().change_scene_to_file("res://scenes/market.tscn")
