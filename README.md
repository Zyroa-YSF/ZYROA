# README progress update

Progress update: started gameplay implementation and HUD.

Files added in this commit:
- scenes/player.tscn
- scenes/npc.tscn
- scenes/hud.tscn
- scripts/ui.gd
- updated scripts/player.gd (interaction and attack signals, HUD integration)

Next steps (immediate):
- Wire HUD as an Autoload or add to main.tscn so it is accessible via /root/HUD.
- Create a simple main.tscn that instances Player and HUD to test movement and interactions locally.
- Start downloading models/sounds and add them to assets/ with correct attributions.
