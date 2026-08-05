extends Node2D

var vendor_sees := false
var vendor_chasing := false
var player_ref := null

func _ready():
    $VendorArea.connect("body_entered", Callable(self, "_on_vendor_area"))

func _on_vendor_area(body):
    if body.name == "Player":
        player_ref = body
        # Offer free simit (placeholder): set a flag on player to allow steal interaction
        player_ref.set_meta("can_steal_simit", true)

func attempt_steal(player):
    # If vendor sees you (line-of-sight or flag), start chase, else give simit
    if vendor_sees:
        # vendor chases forever
        vendor_chasing = true
        # spawn a chasing NPC and start chase
        var npc = preload("res://scripts/npc.gd").new()
        add_child(npc)
        npc.global_position = Vector2(100,100)
        npc.start_chase(player)
    else:
        # successful steal: give simit (game state change placeholder)
        player.set_meta("has_simit", true)
        # proceed to apartment interior
        get_tree().change_scene_to_file("res://scenes/apartment_interior.tscn")

# External call to set vendor sees (e.g., when player is spotted by vendor)
func set_vendor_sees(v: bool):
    vendor_sees = v
