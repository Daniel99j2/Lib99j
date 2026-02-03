## About
This is a mod used as a base for all my mods. Feel free to use it if you wish.

### Library Features
- VFX including locking camera, overlays, and more
- Server-side gui textures
- Server-side particles
- More...

## Usage
### Gui Utils
- By default, Lib99j generates a font space range of -256 to 256 (Note, when using eg, space=270, it uses a combo of 256 and 14!). Use GuiUtils.getSpace() and related functions
- GuiUtils.nextPage(), GuiUtils.previousPage(), and GuiUtils.head() create player heads for use in guis
- GuiUtils.doesPlayerHaveMods() allows you to see what mods are installed, read the documentation for more info
- GuiUtils.toast() displays an advancement popup with a custom item and text
- GuiUtils.colourText() colours an entire section of text
- **Now for the meat of it!**
- GuiUtils.generateTexture() and GuiUtils.generateColourableTexture() creates a GuiElementBuilder that has an auto generated item model and definition. Texture format: "examplemod", "ui/test/image". Note that other base paths are accepted, but it is recommended to use /ui/ as other functions use this solely
- GuiUtils.generateBarTexture() creates a vertical bottom to top filling bar with a base, fill, and overlay.
- BackgroundTextures are a basic inventory-based font background generator, eg: new BackgroundTexture(Identifier.fromNamespaceAndPath(MagmaNetworkMod.MOD_ID, "test/test_ui"), 176). Uses /ui/ for texture references.
- 

### VFX
- VfxUtils.shake() causes a camerashake effect similar to bedrock edition
- VfxUtils.addGenericScreenEffect() adds many other client-side effect such as:
- RED_TINT - Red worldborder tint
- SNOW - Powder snow overlay
- FIRE - Self-explanatory
- NAUSEA - You get it.
- BLACK_HEARTS - Withered hearts
- GREEN_HEARTS - Poisoned hearts
- GREEN_HUNGER - Green hunger haunches
- BLINDNESS - The blindness effect
- NIGHT_VISION - The night vision effect
- DARKNESS - Warden darkness effect
- LOCK_CAMERA_AND_POS - More complicated, explained next.
- 
- **Camera Locking**
- To lock the player's camera and movement, add the LOCK_CAMERA_AND_POS effect
- setCameraInterpolation, setCameraPos, setCameraPitch, and setCameraYaw control varius aspects of the locked camera
- Due to technical reasons, the player is unable to see the hotbar, and the game looks like it is in spectator mode
- 
- **MISC**
- VfxUtils.clientSideExplode() causes a client-side explosion destroying blocks (ONLY ON THE CLIENT). Use only for cinematics, as ghost blocks are buggy
- VfxUtils.fireworkExplode() creates a firework explosion as if a firework actually spawned
- VfxUtils.sendFakeEntity() sends a client side entity. Note it will never be removed normally, so it is only recommended to use this with lightning bolts or other entities that will be auto-removed
- 

### Particles
WORK IN PROGRESS FEATURE, may experience heavy server lag.
Rework will happen soon when I have time.
### Ponder
WORK IN PROGRESS FEATURE!
To put it simply, It's a recreation of Create's Ponder system, but serverside.
### Misc
- PlayPacketUtils contains a list of all PLAY packets
- ConfigUtils has some basic configuration functions
- GameProperties has some toggles and information about the game