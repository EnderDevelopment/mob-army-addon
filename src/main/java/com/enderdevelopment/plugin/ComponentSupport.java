package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

final class ComponentSupport {
    private ComponentSupport() {
    }

    static Component message(String text) {
        return Component.text(text).color(NamedTextColor.GRAY);
    }
}
