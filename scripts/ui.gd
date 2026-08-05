extends CanvasLayer

@onready var prompt := $PromptLabel
@onready var battery_label := $BatteryLabel

func _ready():
    prompt.text = ""
    battery_label.text = ""

func show_prompt(text: String):
    prompt.text = text

func clear_prompt():
    prompt.text = ""

func update_battery(value: float):
    battery_label.text = "Battery: %d%%" % int(value)
