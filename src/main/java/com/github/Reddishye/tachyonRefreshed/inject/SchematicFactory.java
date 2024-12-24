package com.github.Reddishye.tachyonRefreshed.inject;

import com.github.Reddishye.tachyonRefreshed.api.Schematic;
import org.bukkit.Location;

public interface SchematicFactory {
    Schematic create(
        @com.google.inject.assistedinject.Assisted("start") Location start,
        @com.google.inject.assistedinject.Assisted("end") Location end,
        @com.google.inject.assistedinject.Assisted("origin") Location origin
    );
}