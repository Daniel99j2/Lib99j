package com.daniel99j.lib99j.api.gui;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.MiscUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.CustomModelDataTintSource;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unused"})
public class GuiUtils {
    private static final int SPACES_RANGE = 256;
    protected static final List<FontTexture> FONT_TEXTURES = new ArrayList<>();
    private static final Map<Integer, Character> SPACES = new HashMap<>();
    private static final ArrayList<Character> blacklistedChars = new ArrayList<>();
    private static final List<ItemGuiTexture> ITEM_GUI_TEXTURES = new ArrayList<>();
    private static final List<GuiTexture> GUI_TEXTURES = new ArrayList<>();
    private static final List<GuiBarTexture> GUI_BAR_TEXTURES = new ArrayList<>();
    private static char currentGuiChar = '*';
    private static char currentSpaceChar = '*';
    private static final List<String> atlasAdditions = new ArrayList<>();

    private static final String BASIC_ITEM_TEMPLATE = """
            {
              "parent": "%BASE%",
              "textures": {
                "layer0": "%ID%"
              }
            }
            """.replace(" ", "").replace("\n", "");

    static {
        blacklistedChars.add('§');
        blacklistedChars.add('\\');
    }

    @ApiStatus.Internal
    public static void load() {
        currentGuiChar = '*';
        currentSpaceChar = '*';
        blacklistedChars.clear();
        blacklistedChars.add('§');
        blacklistedChars.add('&'); //incase client mods are bad and use & as a formatting symbol
        blacklistedChars.add('\\');
        SPACES.clear();
        for (int i = -SPACES_RANGE; i <= SPACES_RANGE; i++) {
            SPACES.put(i, getNextSpaceChar());
        }
        DefaultGuiTextures.load();
    }

    private static char getNextSpaceChar() {
        char c = currentSpaceChar++;
        if (blacklistedChars.contains(c)) c = getNextSpaceChar();
        return c;
    }

    static char getNextGuiChar() {
        char c = currentGuiChar++;
        if (blacklistedChars.contains(c)) c = getNextGuiChar();
        return c;
    }

    public static MutableComponent getSpace(int pixels) {
        return appendSpace(pixels, null);
    }

    public static MutableComponent appendSpace(int pixels, MutableComponent text) {
        int repeats = Math.floorDiv(pixels, SPACES_RANGE);
        int extra = pixels % SPACES_RANGE;
        for (int i = 0; i < repeats; i++) {
            MutableComponent space = getSpaceThrows(256);
            if(text == null) text = space;
            else text.append(space);
        }

        MutableComponent space = getSpaceThrows(extra);
        if(text == null) text = space;
        else text.append(space);
        return text;
    }

    private static MutableComponent getSpaceThrows(int pixels) {
        if (pixels > SPACES_RANGE || pixels < -SPACES_RANGE)
            throw new IndexOutOfBoundsException("Pixels must be between -" + SPACES_RANGE + " and " + SPACES_RANGE);
        if(pixels == 0) return Component.empty();
        return Component.nullToEmpty(Character.toString(SPACES.get(pixels))).copy().withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "spaces"))));
    }

    public static GuiElementBuilder nextPage(boolean allowed) {
        return (allowed ? DefaultGuiTextures.HEAD_NEXT_PAGE : DefaultGuiTextures.HEAD_NEXT_PAGE_BLOCKED).setName(Component.nullToEmpty("Next Page"));
    }

    public static GuiElementBuilder previousPage(boolean allowed) {
        return (allowed ? DefaultGuiTextures.HEAD_PREVIOUS_PAGE : DefaultGuiTextures.HEAD_PREVIOUS_PAGE_BLOCKED).setName(Component.nullToEmpty("Previous Page"));
    }

    @ApiStatus.Internal
    public static List<ItemGuiTexture> getItemGuiTextures() {
        return ITEM_GUI_TEXTURES;
    }

    @ApiStatus.Internal
    public static void generateAssets(BiConsumer<String, byte[]> assetWriter) {
        var fontBase = new JsonObject();
        var providers = new JsonArray();

        FONT_TEXTURES.forEach((entry) -> {
            var bitmap = new JsonObject();
            bitmap.addProperty("type", "bitmap");
            bitmap.addProperty("file", entry.path() + ".png");
            bitmap.addProperty("ascent", entry.ascent());
            bitmap.addProperty("height", entry.height());
            var chars = new JsonArray();

            for (var a : entry.chars()) {
                var builder = new StringBuilder();
                for (var b : a) {
                    builder.append(b);
                }
                chars.add(builder.toString());
            }

            bitmap.add("chars", chars);
            providers.add(bitmap);
        });

        GUI_BAR_TEXTURES.forEach((entry) -> {
            for (GuiBarTexturePart part : entry.textures()) {
                assetWriter.accept("assets/" + part.texture().path.getNamespace() + "/textures/ui/" + part.texture().path.getPath() + ".png", part.imageData());
            }
        });

        fontBase.add("providers", providers);

        assetWriter.accept("assets/" + Lib99j.MOD_ID + "/font/ui.json", fontBase.toString().getBytes(StandardCharsets.UTF_8));

        var spaceFontBase = new JsonObject();
        var spaceProviders = new JsonArray();


        var spaces = new JsonObject();
        spaces.addProperty("type", "space");
        var advances = new JsonObject();
        SPACES.forEach((integer, character) -> advances.addProperty(Character.toString(character), integer));
        spaces.add("advances", advances);
        spaceProviders.add(spaces);

        spaceFontBase.add("providers", spaceProviders);

        assetWriter.accept("assets/" + Lib99j.MOD_ID + "/font/spaces.json", spaceFontBase.toString().getBytes(StandardCharsets.UTF_8));

        String itemAtlas = "{\"sources\":[{\"type\":\"directory\",\"source\":\"ui\",\"prefix\":\"ui/\"}";

        for (String s : atlasAdditions) {
            itemAtlas += ",{\"type\":\"single\",\"resource\":\"%name%\"}".replace("%name%", s);
        }
        itemAtlas += "]}";

        assetWriter.accept("assets/minecraft/atlases/items.json", itemAtlas.getBytes(StandardCharsets.UTF_8));

        //dont use addBridgedModelsFolder so that we can have finer control, and only include necessary assets!
        for (ItemGuiTexture texture : GuiUtils.getItemGuiTextures()) {
            String baseAndPath = texture.base() + "/" + texture.path().getPath();
            assetWriter.accept("assets/" + texture.path().getNamespace() + "/models/" + baseAndPath + ".json",
                    BASIC_ITEM_TEMPLATE.replace("%ID%", Identifier.fromNamespaceAndPath(texture.path().getNamespace(), baseAndPath).toString()).replace("%BASE%", "minecraft:item/generated").getBytes(StandardCharsets.UTF_8));

            if(texture.coloured()) assetWriter.accept("assets/" + texture.path().getNamespace() + "/items/gen/" + baseAndPath + ".json", new ItemAsset(new BasicItemModel(Identifier.fromNamespaceAndPath(texture.path().getNamespace(), baseAndPath), List.of(new CustomModelDataTintSource(0, 0xFFFFFF)))).toJson().getBytes());
            else assetWriter.accept("assets/" + texture.path().getNamespace() + "/items/gen/" + texture.base() + "/" + texture.path().getPath() + ".json", new ItemAsset(new BasicItemModel(Identifier.fromNamespaceAndPath(texture.path().getNamespace(), baseAndPath), List.of())).toJson().getBytes());

        }
    }

    public static GuiElementBuilder generateTexture(Identifier path) {
        return generateTextureInternal(path, false);
    }

    public static GuiElementBuilder generateColourableTexture(Identifier path) {
        return generateTextureInternal(path, true);
    }

    private static GuiElementBuilder generateTextureInternal(Identifier path, boolean coloured) {
        String base = MiscUtils.getTextBetween(path.getPath(), "", "/");
        if(base.isEmpty()) throw new IllegalStateException("Error loading "+path.toString(), new Throwable("Texture paths must be prefixed, eg ui/test.png"));
        Identifier newPath = Identifier.fromNamespaceAndPath(path.getNamespace(), MiscUtils.replaceTextBetween(path.getPath(), "", "/", ""));
        ItemGuiTexture texture = new ItemGuiTexture(newPath, base, coloured);
        ITEM_GUI_TEXTURES.add(texture);
        if(!base.equals("ui")) atlasAdditions.add(path.toString());
        return blank().model(Identifier.fromNamespaceAndPath(newPath.getNamespace(), "gen/" + base + "/" + newPath.getPath())).setItemName(Component.nullToEmpty("==NOT SET=="));
    }

    public static MutableComponent colourText(MutableComponent texts, int colour) {
        MutableComponent newText = Component.literal(texts.toString()).withStyle(texts.getStyle()).withColor(colour);
        for (Component text : texts.getSiblings()) {
            MutableComponent text1 = null;
            if(text instanceof MutableComponent) text1 = (MutableComponent) text;
            else text1 = Component.literal(text1.getString()).withStyle(text1.getStyle());
            newText.append(text1.withColor(colour));
        }
        return newText;
    }

    public static MutableComponent styleText(MutableComponent texts, Style style) {
        MutableComponent newText = Component.literal(texts.toString()).withStyle(style);
        for (Component text : texts.getSiblings()) {
            MutableComponent text1 = null;
            if(text instanceof MutableComponent) text1 = (MutableComponent) text;
            else text1 = Component.literal(text1.getString()).withStyle(style);
            newText.append(text1);
        }
        return newText;
    }

    public static GuiElementBuilder blank() {
        return GuiElementBuilder.from(Items.BARRIER.getDefaultInstance()).noDefaults().setMaxCount(1);
    }

    public static GuiElementBuilder head(String texture) {
        return GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultInstance()).noDefaults().setMaxCount(1).setSkullOwner(texture).setItemName(Component.nullToEmpty("==NOT SET=="));
    }

    public static GuiBarTexture generateBarTexture(Identifier path, int ascent, int height1, int width1) {
        try {
            ArrayList<GuiBarTexturePart> textures = new ArrayList<>();
            InputStream stream = Lib99j.class.getResourceAsStream("/assets/" + path.getNamespace() + "/textures/ui/" + path.getPath() + ".png");
            BufferedImage image = ImageIO.read(stream);
            int width = image.getWidth();
            int height = image.getHeight();
            int i2 = 0;

            for (int i = 0; i < height; i++) {
                BufferedImage slice = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y <= i; y++) {
                    slice.setRGB(0, height - i + y - 1, width, 1,
                            image.getRGB(0, height - i + y - 1, width, 1, null, 0, width),
                            0, width
                    );
                }

                //this fixes minecraft not making the font the full image by 'expanding' the texture!
                int[] corners = {
                        slice.getRGB(0, 0),
                        slice.getRGB(width - 1, 0),
                        slice.getRGB(0, height - 1),
                        slice.getRGB(width - 1, height - 1)
                };

                for (int cornerIdx = 0; cornerIdx < 4; cornerIdx++) {
                    if ((corners[cornerIdx] >> 24) == 0) {  // fully transparent
                        int x = (cornerIdx % 2 == 0) ? 0 : width - 1;
                        int y = (cornerIdx < 2) ? 0 : height - 1;
                        slice.setRGB(x, y, 0x1CFFFFFF); //almost transparent white
                    }
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(slice, "PNG", baos);
                byte[] imageData = baos.toByteArray();

                String outputPath = path.getPath() + "_gen_" + i;

                GuiTexture texture = new GuiTexture(Identifier.fromNamespaceAndPath(path.getNamespace(), outputPath), ascent, height1, width1);
                textures.add(new GuiBarTexturePart(texture, imageData));
                GUI_TEXTURES.add(texture);
                i2++;
            }

            if (i2 > height) {
                throw new IndexOutOfBoundsException();
            }

            GuiBarTexture out = new GuiBarTexture(textures, ascent, height1);
            GUI_BAR_TEXTURES.add(out);

            return out;
        } catch (Exception e) {
            Lib99j.LOGGER.error("Error loading bar texture: " + e);
        }
        return null;
    }

    public static boolean isNumberKey(ClickType clickType) {
        return getNumberKeyValue(clickType) != -1;
    }

    public static int getNumberKeyValue(ClickType clickType) {
        return switch (clickType) {
            case ClickType.NUM_KEY_1 -> 1;
            case ClickType.NUM_KEY_2 -> 2;
            case ClickType.NUM_KEY_3 -> 3;
            case ClickType.NUM_KEY_4 -> 4;
            case ClickType.NUM_KEY_5 -> 5;
            case ClickType.NUM_KEY_6 -> 6;
            case ClickType.NUM_KEY_7 -> 7;
            case ClickType.NUM_KEY_8 -> 8;
            case ClickType.NUM_KEY_9 -> 9;
            default -> -1;
        };
    }

    /**
     * Return a list of translations that exist on a player's client
     * <p>Utilizes <a href="https://mojira.dev/MC-265322">MC-265322</a> to get translations
     * <p>The check will normally take ~1 tick to check, however this depends on ping and if other checks are running
     *
     * @param modTranslations A Map of modId -> translation to check
     * @param output A consumer of PlayerTranslationsResponse. This contains mods that were on the client, weren't on the client, if the check was not blocked by the client's mods (eg, hacks can disable this as it can expose them), and if the check failed over 5 times (the client never responded...)
     */
    public static void doesPlayerHaveMods(ServerPlayer player, Map<String, String> modTranslations, Consumer<PlayerTranslationsResponse> output) {
        ((Lib99jPlayerUtilController) player).lib99j$addTranslationChecker(modTranslations, output);
    }

    public static boolean isGeneric54Screen(MenuType screenHandlerType) {
        return
                (screenHandlerType == MenuType.GENERIC_9x1 ||
                        screenHandlerType == MenuType.GENERIC_9x2 ||
                        screenHandlerType == MenuType.GENERIC_9x3 ||
                        screenHandlerType == MenuType.GENERIC_9x4 ||
                        screenHandlerType == MenuType.GENERIC_9x5 ||
                        screenHandlerType == MenuType.GENERIC_9x6);
    }

    public static void basicGuiBackground(SimpleGui gui) {
        basicGuiBackground(gui, false);
    }

    public static void basicGuiBackground(SimpleGui gui, boolean showSlotIds) {
        for (int i = 0; i < gui.getSize(); i++) {
            ItemStack stack;
            if (showSlotIds) {
                stack = Items.GLASS_PANE.getDefaultInstance();
                stack.set(DataComponents.MAX_DAMAGE, gui.getSize());
                stack.set(DataComponents.DAMAGE, gui.getSize() - i);
            } else {
                stack = DefaultGuiTextures.INVISIBLE.getItemStack();
            }

            if (gui.getSlotRedirect(i) != null) continue;
            if (gui.getSlot(i) != null && gui.getSlot(i).getItemStack() != null && !gui.getSlot(i).getItemStack().isEmpty()) continue;
            gui.setSlot(i, stack);
        }
    }

    public static void toast(ServerPlayer player, ItemStack icon, Component title, Identifier background) {
        AdvancementProgress progress = new AdvancementProgress();
        progress.update(AdvancementRequirements.allOf(Set.of("toast")));
        progress.grantProgress("toast");
        player.connection.send(new ClientboundUpdateAdvancementsPacket(false, Set.of(new AdvancementHolder(Identifier.fromNamespaceAndPath("lib99j", "toast"), new Advancement(Optional.empty(), Optional.of(new DisplayInfo(icon, title, Component.empty(), Optional.of(new ClientAsset.ResourceTexture(background)), AdvancementType.TASK, true, false, false)), AdvancementRewards.EMPTY, Map.of("toast", PlayerTrigger.TriggerInstance.tick()), AdvancementRequirements.allOf(Set.of("toast")), false, Optional.of(title)))), Set.of(), Map.of(), true));
        player.connection.send(new ClientboundUpdateAdvancementsPacket(false, Set.of(), Set.of(), Map.of(Identifier.fromNamespaceAndPath("lib99j", "toast"), progress), true));
        player.connection.send(new ClientboundUpdateAdvancementsPacket(false, Set.of(), Set.of(Identifier.fromNamespaceAndPath("lib99j", "toast")), Map.of(), false));
    }

    public record PlayerTranslationsResponse(boolean translationCheckBlocked, boolean checkFailed, ArrayList<String> matches, ArrayList<String> misses) {
    }

    @ApiStatus.Internal
    public record PlayerTranslationCheckerData(List<Map.Entry<String, String>> translations, List<String> results, Consumer<GuiUtils.PlayerTranslationsResponse> output, MutableInt remainingTries) {

    }
}
