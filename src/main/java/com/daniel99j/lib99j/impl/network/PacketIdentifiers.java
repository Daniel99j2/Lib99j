package com.daniel99j.lib99j.impl.network;

import com.daniel99j.lib99j.Lib99j;
import net.minecraft.resources.Identifier;

public class PacketIdentifiers {
    public static final Identifier SERVERBOUND_HELLO = Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "hi_is_lib99j_installed_question_mark");
    public static final Identifier CLIENTBOUND_HELLO = Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "yes_lib99j_is_installed");

    public static final Identifier SERVERBOUND_LIB99J_INSTALLED_MODS = Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "serverbound_installed_mods");

    public static final Identifier CLIENTBOUND_CONFIG_OPTION_SYNC = Identifier.fromNamespaceAndPath(Lib99j.MOD_ID, "clientbound_sync_config");
}
