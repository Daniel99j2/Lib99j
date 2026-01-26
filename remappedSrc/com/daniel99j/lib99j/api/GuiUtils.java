package com.daniel99j.lib99j.api;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.core.mixin.block.BlockEntityUpdateS2CPacketAccessor;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
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
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
    private static final List<FontTexture> FONT_TEXTURES = new ArrayList<>();
    private static final Map<Integer, Character> SPACES = new HashMap<>();
    private static final ArrayList<Character> blacklistedChars = new ArrayList<>();
    private static final List<GuiTextures.ItemGuiTexture> ITEM_GUI_TEXTURES = new ArrayList<>();
    private static final List<GuiTextures.GuiTexture> GUI_TEXTURES = new ArrayList<>();
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
        blacklistedChars.add('\\');
        SPACES.clear();
        for (int i = -SPACES_RANGE; i <= SPACES_RANGE; i++) {
            SPACES.put(i, getNextSpaceChar());
        }
        GuiTextures.load();

        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(Lib99j.MOD_ID, "gui"));
    }

    private static char getNextSpaceChar() {
        char c = currentSpaceChar++;
        if (blacklistedChars.contains(c)) c = getNextSpaceChar();
        return c;
    }

    private static char getNextGuiChar() {
        char c = currentGuiChar++;
        if (blacklistedChars.contains(c)) c = getNextGuiChar();
        return c;
    }

    public static MutableText getSpace(int pixels) {
        return getSpace(pixels, Text.literal(""));
    }

    public static MutableText getSpace(int pixels, MutableText text) {
        if (pixels > SPACES_RANGE || pixels < -SPACES_RANGE)
            throw new IndexOutOfBoundsException("Pixels must be between -" + SPACES_RANGE + " and " + SPACES_RANGE);
        text.append(Text.of(Character.toString(SPACES.get(pixels))).copy().fillStyle(Style.EMPTY.withFont(Identifier.of(Lib99j.MOD_ID, "spaces"))));
        return text;
    }

    public static GuiElementBuilder nextPage(boolean allowed) {
        return (allowed ? GuiTextures.HEAD_NEXT_PAGE : GuiTextures.HEAD_NEXT_PAGE_BLOCKED).setName(Text.of("Next Page"));
    }

    public static GuiElementBuilder previousPage(boolean allowed) {
        return (allowed ? GuiTextures.HEAD_PREVIOUS_PAGE : GuiTextures.HEAD_PREVIOUS_PAGE_BLOCKED).setName(Text.of("Previous Page"));
    }

    @ApiStatus.Internal
    public static List<GuiTextures.ItemGuiTexture> getItemGuiTextures() {
        return ITEM_GUI_TEXTURES;
    }

    @ApiStatus.Internal
    public static void generateAssets(BiConsumer<String, byte[]> assetWriter) {
        var fontBase = new JsonObject();
        var providers = new JsonArray();

        FONT_TEXTURES.forEach((entry) -> {
            var bitmap = new JsonObject();
            bitmap.addProperty("type", "bitmap");
            bitmap.addProperty("file", entry.path + ".png");
            bitmap.addProperty("ascent", entry.ascent);
            bitmap.addProperty("height", entry.height);
            var chars = new JsonArray();

            for (var a : entry.chars) {
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
            for (GuiBarTexturePart part : entry.textures) {
                assetWriter.accept("assets/" + part.texture.path.getNamespace() + "/textures/gui/" + part.texture.path.getPath() + ".png", part.imageData());
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
        GuiTextures.ItemGuiTexture texture = new GuiTextures.ItemGuiTexture(path);
        ITEM_GUI_TEXTURES.add(texture);
        ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(path.getNamespace(), "gui"));
        return GuiElementBuilder.from(Items.BARRIER.getDefaultStack()).noDefaults().setMaxCount(1).model(Identifier.of(path.getNamespace(), "-/gui/" + path.getPath())).setItemName(Text.of("==NOT SET=="));
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

                GuiTextures.GuiTexture texture = new GuiTextures.GuiTexture(Identifier.of(path.getNamespace(), outputPath), ascent, height1, width1);
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
     * Return a list of mods that are on a player's client
     *
     * @param modTranslations A Map of modId -> translation to check
     */
    public static void doesPlayerHaveMods(ServerPlayerEntity player, Map<String, String> modTranslations, Consumer<ArrayList<String>> output) {
        ArrayList<Map.Entry<String, String>> entries = new ArrayList<>(modTranslations.entrySet());
        ((Lib99jPlayerUtilController) player).lib99j$setModCheckerOutput(output);
        ((Lib99jPlayerUtilController) player).lib99j$setNeededModCheckerTranslations(entries);
        int total = entries.size();
        int lines = 3;
        int count = (int) Math.ceil((double) total / lines);
        BlockPos pos = new BlockPos(player.getBlockPos().getX(), player.getWorld().getBottomY(), player.getBlockPos().getZ());

        for (int i = 0; i < count; i++) {
            NbtCompound nbt = new NbtCompound();
            SignText text = new SignText();

            for (int line = 0; line < lines; line++) {
                int index = i * lines + line;
                if (index < total) {
                    Map.Entry<String, String> entry = entries.get(index);
                    text = text.withMessage(line, Text.translatable(entry.getValue()).withColor(entries.indexOf(entry)));
                }
            }

            if (i == count - 1) text = text.withMessage(3, Text.literal("lib99j$final"));
            else text = text.withMessage(3, Text.literal("lib99j$checker"));

            DynamicOps<NbtElement> dynamicOps = player.getRegistryManager().getOps(NbtOps.INSTANCE);
            nbt.put("front_text", SignText.CODEC, dynamicOps, text);
            nbt.put("back_text", SignText.CODEC, dynamicOps, new SignText());
            nbt.putBoolean("is_waxed", false);

            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, Blocks.OAK_SIGN.getDefaultState()));
            player.networkHandler.sendPacket(BlockEntityUpdateS2CPacketAccessor.createBlockEntityUpdateS2CPacket(pos, BlockEntityType.SIGN, nbt));
            player.networkHandler.sendPacket(new SignEditorOpenS2CPacket(pos, true));
            player.networkHandler.sendPacket(new CloseScreenS2CPacket(0));
        }
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, player.getWorld().getBlockState(pos)));
        if (player.getWorld().getBlockEntity(pos) != null)
            player.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(player.getWorld().getBlockEntity(pos)));
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
                stack = GuiUtils.GuiTextures.INVISIBLE.getItemStack();
            }
            if (gui.getSlot(i) == null) gui.setSlot(i, stack);
        }
    }

    public static void toast(ServerPlayerEntity player, ItemStack item) {
        ArrayList<RecipeBookAddS2CPacket.Entry> entries = new ArrayList<>();
        entries.add(new RecipeBookAddS2CPacket.Entry(new RecipeDisplayEntry(new NetworkRecipeId(-5957), new ShapelessCraftingRecipeDisplay(List.of(SlotDisplay.EmptySlotDisplay.INSTANCE), new SlotDisplay.StackSlotDisplay(item), SlotDisplay.EmptySlotDisplay.INSTANCE), OptionalInt.empty(), RecipeBookCategories.CRAFTING_MISC, Optional.empty()), true, false));
        player.networkHandler.sendPacket(new RecipeBookAddS2CPacket(entries, false));
        player.networkHandler.sendPacket(new RecipeBookRemoveS2CPacket(List.of(new NetworkRecipeId(-5957))));
    }

    public static class BackgroundTexture {
        final char character = getNextGuiChar();
        final int width;
        final Identifier path;

        public BackgroundTexture(Identifier path, int width) {
            this.width = width;
            this.path = path;
            ResourcePackExtras.forDefault().addBridgedModelsFolder(Identifier.of(path.getNamespace(), "gui"));
            FONT_TEXTURES.add(new FontTexture(Identifier.of(path.getNamespace(), "gui/" + path.getPath()), 13, 256, new char[][]{new char[]{character}}));
        }

        public MutableText text() {
            MutableText text = getSpace(-8, Text.literal(""));
            text.append(Text.literal(Character.toString(character)).formatted(Formatting.WHITE).fillStyle(Style.EMPTY.withFont(Identifier.of(path.getNamespace(), "gui"))));
            getSpace(8, text);
            getSpace(-width, text);
            return text;
        }
    }

    public record FontTexture(Identifier path, int ascent, int height, char[][] chars) {

    }

    public record GuiBarTexture(ArrayList<GuiBarTexturePart> textures, int ascent, int height) {
    }

    public record GuiBarTexturePart(GuiTextures.GuiTexture texture, byte[] imageData) {
    }

    public static class GuiTextures {
        public static final GuiElement INVISIBLE = generateTexture(Identifier.of(Lib99j.MOD_ID, "empty_slot")).setItemName(Text.of("")).hideTooltip().build();

        //textures from polydex
        public static final GuiElementBuilder HEAD_PREVIOUS_PAGE = head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEwODI5OGZmMmIyNjk1MWQ2ODNlNWFkZTQ2YTQyZTkwYzJmN2M3ZGQ0MWJhYTkwOGJjNTg1MmY4YzMyZTU4MyJ9fX0");
        public static final GuiElementBuilder HEAD_PREVIOUS_PAGE_BLOCKED = head("ewogICJ0aW1lc3RhbXAiIDogMTY0MDYxNjE5MjE0MiwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MDgyMGY3NmUzZTA0MWM3NWY3NmQwZjMwMTIzMmJkZjQ4MzIxYjUzNGZlNmE4NTljY2I4NzNkMjk4MWE5NjIzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
        public static final GuiElementBuilder HEAD_NEXT_PAGE = head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ");
        public static final GuiElementBuilder HEAD_NEXT_PAGE_BLOCKED = head("ewogICJ0aW1lc3RhbXAiIDogMTY0MDYxNjExMDQ4OCwKICAicHJvZmlsZUlkIiA6ICIxZjEyNTNhYTVkYTQ0ZjU5YWU1YWI1NmFhZjRlNTYxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RNaUt5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdlNTc3MjBhNDg3OGM4YmNhYjBlOWM5YzQ3ZDllNTUxMjhjY2Q3N2JhMzQ0NWE1NGE5MWUzZTFlMWEyNzM1NmUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        public static final GuiElementBuilder HEAD_ADD = head("ewogICJ0aW1lc3RhbXAiIDogMTY0MjM2Mzc1NDQxMCwKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80OTQ0YTI5ZjY4Yzg4NmNmYWY2N2UxNTI1YmQyYWNjMmEzZDRlNDBjMDE3NzVjNzIyMTQwZjA4YjY5ZDVkNjliIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");


        private static void load() {
        }

        public record ItemGuiTexture(Identifier path) {
        }

        public static class GuiTexture {
            public final Identifier path;
            public final int ascent;
            public final int height;
            public final int width;
            final char character;

            public GuiTexture(Identifier path, int ascent, int height, int width) {
                this.ascent = ascent;
                this.path = path;
                this.height = height;
                this.width = width;
                this.character = getNextGuiChar();
                FONT_TEXTURES.add(new FontTexture(Identifier.of(path.getNamespace(), "gui/" + path.getPath()), ascent, height, new char[][]{new char[]{character}}));
            }

            public MutableText text() {
                MutableText text = Text.literal(Character.toString(character)).formatted(Formatting.WHITE).fillStyle(Style.EMPTY.withFont(Identifier.of(path.getNamespace(), "gui")));
                getSpace(-width, text);
                return text;
            }
        }
    }
}
