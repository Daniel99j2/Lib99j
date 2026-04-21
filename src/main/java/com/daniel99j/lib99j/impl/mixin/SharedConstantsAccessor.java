package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SharedConstants.class)
public interface SharedConstantsAccessor {
    @Mutable @Accessor("DEBUG_OPEN_INCOMPATIBLE_WORLDS")
    static void DEBUG_OPEN_INCOMPATIBLE_WORLDS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_ALLOW_LOW_SIM_DISTANCE")
    static void DEBUG_ALLOW_LOW_SIM_DISTANCE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_HOTKEYS")
    static void DEBUG_HOTKEYS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_UI_NARRATION")
    static void DEBUG_UI_NARRATION(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SHUFFLE_UI_RENDERING_ORDER")
    static void DEBUG_SHUFFLE_UI_RENDERING_ORDER(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SHUFFLE_MODELS")
    static void DEBUG_SHUFFLE_MODELS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_RENDER_UI_LAYERING_RECTANGLES")
    static void DEBUG_RENDER_UI_LAYERING_RECTANGLES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_PATHFINDING")
    static void DEBUG_PATHFINDING(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES")
    static void DEBUG_SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SHAPES")
    static void DEBUG_SHAPES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_NEIGHBORSUPDATE")
    static void DEBUG_NEIGHBORSUPDATE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER")
    static void DEBUG_EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_STRUCTURES")
    static void DEBUG_STRUCTURES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_GAME_EVENT_LISTENERS")
    static void DEBUG_GAME_EVENT_LISTENERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DUMP_TEXTURE_ATLAS")
    static void DEBUG_DUMP_TEXTURE_ATLAS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_STRUCTURE_EDIT_MODE")
    static void DEBUG_STRUCTURE_EDIT_MODE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SAVE_STRUCTURES_AS_SNBT")
    static void DEBUG_SAVE_STRUCTURES_AS_SNBT(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SYNCHRONOUS_GL_LOGS")
    static void DEBUG_SYNCHRONOUS_GL_LOGS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_VERBOSE_SERVER_EVENTS")
    static void DEBUG_VERBOSE_SERVER_EVENTS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_NAMED_RUNNABLES")
    static void DEBUG_NAMED_RUNNABLES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_GOAL_SELECTOR")
    static void DEBUG_GOAL_SELECTOR(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_VILLAGE_SECTIONS")
    static void DEBUG_VILLAGE_SECTIONS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_BRAIN")
    static void DEBUG_BRAIN(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_POI")
    static void DEBUG_POI(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_BEES")
    static void DEBUG_BEES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_RAIDS")
    static void DEBUG_RAIDS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_BLOCK_BREAK")
    static void DEBUG_BLOCK_BREAK(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_MONITOR_TICK_TIMES")
    static void DEBUG_MONITOR_TICK_TIMES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_KEEP_JIGSAW_BLOCKS_DURING_STRUCTURE_GEN")
    static void DEBUG_KEEP_JIGSAW_BLOCKS_DURING_STRUCTURE_GEN(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DONT_SAVE_WORLD")
    static void DEBUG_DONT_SAVE_WORLD(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_LARGE_DRIPSTONE")
    static void DEBUG_LARGE_DRIPSTONE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_CARVERS")
    static void DEBUG_CARVERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_ORE_VEINS")
    static void DEBUG_ORE_VEINS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SCULK_CATALYST")
    static void DEBUG_SCULK_CATALYST(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_BYPASS_REALMS_VERSION_CHECK")
    static void DEBUG_BYPASS_REALMS_VERSION_CHECK(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SOCIAL_INTERACTIONS")
    static void DEBUG_SOCIAL_INTERACTIONS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_CHAT_DISABLED")
    static void DEBUG_CHAT_DISABLED(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_VALIDATE_RESOURCE_PATH_CASE")
    static void DEBUG_VALIDATE_RESOURCE_PATH_CASE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_UNLOCK_ALL_TRADES")
    static void DEBUG_UNLOCK_ALL_TRADES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_BREEZE_MOB")
    static void DEBUG_BREEZE_MOB(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_TRIAL_SPAWNER_DETECTS_SHEEP_AS_PLAYERS")
    static void DEBUG_TRIAL_SPAWNER_DETECTS_SHEEP_AS_PLAYERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_VAULT_DETECTS_SHEEP_AS_PLAYERS")
    static void DEBUG_VAULT_DETECTS_SHEEP_AS_PLAYERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_FORCE_ONBOARDING_SCREEN")
    static void DEBUG_FORCE_ONBOARDING_SCREEN(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_CURSOR_POS")
    static void DEBUG_CURSOR_POS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DEFAULT_SKIN_OVERRIDE")
    static void DEBUG_DEFAULT_SKIN_OVERRIDE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_PANORAMA_SCREENSHOT")
    static void DEBUG_PANORAMA_SCREENSHOT(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_CHASE_COMMAND")
    static void DEBUG_CHASE_COMMAND(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_VERBOSE_COMMAND_ERRORS")
    static void DEBUG_VERBOSE_COMMAND_ERRORS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DEV_COMMANDS")
    static void DEBUG_DEV_COMMANDS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_ACTIVE_TEXT_AREAS")
    static void DEBUG_ACTIVE_TEXT_AREAS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_PREFER_WAYLAND")
    static void DEBUG_PREFER_WAYLAND(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_IGNORE_LOCAL_MOB_CAP")
    static void DEBUG_IGNORE_LOCAL_MOB_CAP(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_LIQUID_SPREADING")
    static void DEBUG_DISABLE_LIQUID_SPREADING(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_AQUIFERS")
    static void DEBUG_AQUIFERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_JFR_PROFILING_ENABLE_LEVEL_LOADING")
    static void DEBUG_JFR_PROFILING_ENABLE_LEVEL_LOADING(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_ENTITY_BLOCK_INTERSECTION")
    static void DEBUG_ENTITY_BLOCK_INTERSECTION(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_ONLY_GENERATE_HALF_THE_WORLD")
    static void DEBUG_ONLY_GENERATE_HALF_THE_WORLD(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_FLUID_GENERATION")
    static void DEBUG_DISABLE_FLUID_GENERATION(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_AQUIFERS")
    static void DEBUG_DISABLE_AQUIFERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_SURFACE")
    static void DEBUG_DISABLE_SURFACE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_CARVERS")
    static void DEBUG_DISABLE_CARVERS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_STRUCTURES")
    static void DEBUG_DISABLE_STRUCTURES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_FEATURES")
    static void DEBUG_DISABLE_FEATURES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_ORE_VEINS")
    static void DEBUG_DISABLE_ORE_VEINS(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_BLENDING")
    static void DEBUG_DISABLE_BLENDING(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DISABLE_BELOW_ZERO_RETROGENERATION")
    static void DEBUG_DISABLE_BELOW_ZERO_RETROGENERATION(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SUBTITLES")
    static void DEBUG_SUBTITLES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("COMMAND_STACK_TRACES")
    static void COMMAND_STACK_TRACES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_WORLD_RECREATE")
    static void DEBUG_WORLD_RECREATE(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_SHOW_SERVER_DEBUG_VALUES")
    static void DEBUG_SHOW_SERVER_DEBUG_VALUES(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_FEATURE_COUNT")
    static void DEBUG_FEATURE_COUNT(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_FORCE_TELEMETRY")
    static void DEBUG_FORCE_TELEMETRY(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_DONT_SEND_TELEMETRY_TO_BACKEND")
    static void DEBUG_DONT_SEND_TELEMETRY_TO_BACKEND(boolean value) { throw new AssertionError(); }

    @Mutable @Accessor("DEBUG_ENABLED")
    static void DEBUG_ENABLED(boolean value) { throw new AssertionError(); }
}