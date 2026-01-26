package com.daniel99j.lib99j.api.gui;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.core.mixin.block.ClientboundBlockEntityDataPacketAccessor;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.advancement.*;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignText;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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

        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(Lib99j.MOD_ID, "gui"));
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

    public static MutableText getSpace(int pixels) {
        return appendSpace(pixels, null);
    }

    public static MutableText appendSpace(int pixels, MutableText text) {
        int repeats = Math.floorDiv(pixels, SPACES_RANGE);
        int extra = pixels % SPACES_RANGE;
        for (int i = 0; i < repeats; i++) {
            MutableText space = getSpaceThrows(256);
            if(text == null) text = space;
            else text.append(space);
        }

        MutableText space = getSpaceThrows(extra);
        if(text == null) text = space;
        else text.append(space);
        return text;
    }

    private static MutableText getSpaceThrows(int pixels) {
        if (pixels > SPACES_RANGE || pixels < -SPACES_RANGE)
            throw new IndexOutOfBoundsException("Pixels must be between -" + SPACES_RANGE + " and " + SPACES_RANGE);
        if(pixels == 0) return Text.empty();
        return Text.of(Character.toString(SPACES.get(pixels))).copy().fillStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of(Lib99j.MOD_ID, "spaces"))));
    }

    public static GuiElementBuilder nextPage(boolean allowed) {
        return (allowed ? DefaultGuiTextures.HEAD_NEXT_PAGE : DefaultGuiTextures.HEAD_NEXT_PAGE_BLOCKED).setName(Text.of("Next Page"));
    }

    public static GuiElementBuilder previousPage(boolean allowed) {
        return (allowed ? DefaultGuiTextures.HEAD_PREVIOUS_PAGE : DefaultGuiTextures.HEAD_PREVIOUS_PAGE_BLOCKED).setName(Text.of("Previous Page"));
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
                assetWriter.accept("assets/" + part.texture().path.getNamespace() + "/textures/gui/" + part.texture().path.getPath() + ".png", part.imageData());
            }
        });

        fontBase.add("providers", providers);

        assetWriter.accept("assets/" + Lib99j.MOD_ID + "/font/gui.json", fontBase.toString().getBytes(StandardCharsets.UTF_8));

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
    }

    public static GuiElementBuilder generateTexture(Identifier path) {
        ItemGuiTexture texture = new ItemGuiTexture(path);
        ITEM_GUI_TEXTURES.add(texture);
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(path.getNamespace(), "gui"));
        return blank().model(Identifier.of(path.getNamespace(), "-/gui/" + path.getPath())).setItemName(Text.of("==NOT SET=="));
    }

    public static GuiElementBuilder generateColourableTexture(Identifier path) {
        ItemGuiTexture texture = new ItemGuiTexture(path);
        //ITEM_GUI_TEXTURES.add(texture);
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(path.getNamespace(), "gui"));
        return blank().model(Identifier.of(path.getNamespace(), "-/gui/" + path.getPath())).setItemName(Text.of("==NOT SET=="));
    }

    public static GuiElementBuilder blank() {
        return GuiElementBuilder.from(Items.BARRIER.getDefaultStack()).noDefaults().setMaxCount(1);
    }

    public static GuiElementBuilder head(String texture) {
        return GuiElementBuilder.from(Items.PLAYER_HEAD.getDefaultStack()).noDefaults().setMaxCount(1).setSkullOwner(texture).setItemName(Text.of("==NOT SET=="));
    }

    public static GuiBarTexture generateBarTexture(Identifier path, int ascent, int height1, int width1) {
        try {
            ArrayList<GuiBarTexturePart> textures = new ArrayList<>();
            InputStream stream = Lib99j.class.getResourceAsStream("/assets/" + path.getNamespace() + "/textures/gui/" + path.getPath() + ".png");
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

                GuiTexture texture = new GuiTexture(Identifier.of(path.getNamespace(), outputPath), ascent, height1, width1);
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
    public static void doesPlayerHaveMods(ServerPlayerEntity player, Map<String, String> modTranslations, Consumer<PlayerTranslationsResponse> output) {
        ((Lib99jPlayerUtilController) player).lib99j$addTranslationChecker(modTranslations, output);
    }

    public static boolean isGeneric54Screen(ScreenHandlerType screenHandlerType) {
        return
                (screenHandlerType == ScreenHandlerType.GENERIC_9X1 ||
                        screenHandlerType == ScreenHandlerType.GENERIC_9X2 ||
                        screenHandlerType == ScreenHandlerType.GENERIC_9X3 ||
                        screenHandlerType == ScreenHandlerType.GENERIC_9X4 ||
                        screenHandlerType == ScreenHandlerType.GENERIC_9X5 ||
                        screenHandlerType == ScreenHandlerType.GENERIC_9X6);
    }

    public static void basicGuiBackground(SimpleGui gui) {
        basicGuiBackground(gui, false);
    }

    public static void basicGuiBackground(SimpleGui gui, boolean showSlotIds) {
        for (int i = 0; i < gui.getSize(); i++) {
            ItemStack stack;
            if (showSlotIds) {
                stack = Items.GLASS_PANE.getDefaultStack();
                stack.set(DataComponentTypes.MAX_DAMAGE, gui.getSize());
                stack.set(DataComponentTypes.DAMAGE, gui.getSize() - i);
            } else {
                stack = DefaultGuiTextures.INVISIBLE.getItemStack();
            }

            if (gui.getSlotRedirect(i) != null) continue;
            if (gui.getSlot(i) != null && gui.getSlot(i).getItemStack() != null && !gui.getSlot(i).getItemStack().isEmpty()) continue;
            gui.setSlot(i, stack);
        }
    }

    public static void toast(ServerPlayerEntity player, ItemStack icon, Text title, Text description, Identifier background) {
        AdvancementProgress progress = new AdvancementProgress();
        progress.init(AdvancementRequirements.allOf(Set.of("toast")));
        progress.obtain("toast");
        player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(false, Set.of(new AdvancementEntry(Identifier.of("lib99j", "toast"), new Advancement(Optional.empty(), Optional.of(new AdvancementDisplay(icon, title, description, Optional.of(new AssetInfo.TextureAssetInfo(background)), AdvancementFrame.TASK, true, false, false)), AdvancementRewards.NONE, Map.of("toast", TickCriterion.Conditions.createTick()), AdvancementRequirements.allOf(Set.of("toast")), false, Optional.of(title)))), Set.of(), Map.of(), true));
        player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(false, Set.of(), Set.of(), Map.of(Identifier.of("lib99j", "toast"), progress), true));
        player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(false, Set.of(), Set.of(Identifier.of("lib99j", "toast")), Map.of(), false));
    }

    public record PlayerTranslationsResponse(boolean translationCheckBlocked, boolean checkFailed, ArrayList<String> matches, ArrayList<String> misses) {
    }

    @ApiStatus.Internal
    public record PlayerTranslationCheckerData(List<Map.Entry<String, String>> translations, List<String> results, Consumer<GuiUtils.PlayerTranslationsResponse> output, MutableInt remainingTries) {

    }
}
