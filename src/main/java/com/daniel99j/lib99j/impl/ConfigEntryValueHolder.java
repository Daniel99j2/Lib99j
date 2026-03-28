package com.daniel99j.lib99j.impl;

import com.daniel99j.lib99j.api.config.ConfigHolder;

public interface ConfigEntryValueHolder {
    void lib99j$setValue(ConfigHolder.ConfigField value);

    ConfigHolder.ConfigField lib99j$getValue();
}
