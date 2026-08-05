extends Node2D

func _ready():
    $Cassette.connect("body_entered", Callable(self, "_on_cassette"))

func _on_cassette(body):
    if body.name == "Player":
        pick_cassette(body)

func pick_cassette(player):
    # Play pickup SFX and trigger final text
    show_ending()

func show_ending():
    # For prototype, print ending to console and display a simple label (UI not implemented in text scene)
    print("SON SATICIYI KIMSE BULMADI. EVE GITTIN VE OYUNU OYNADIN... POLISLER EVINI BASTI VE OMUR BOYU HAPIS CEZASI ALDIN")
