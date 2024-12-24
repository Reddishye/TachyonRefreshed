package com.github.Reddishye.tachyonRefreshed.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.github.Reddishye.tachyonRefreshed.api.Schematic;
import com.github.Reddishye.tachyonRefreshed.api.SchematicService;
import com.github.Reddishye.tachyonRefreshed.packet.BlockChangePacketSender;
import org.bukkit.Location;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Singleton
public class TachyonSchematicService implements SchematicService {
    private final BlockChangePacketSender packetSender;

    @Inject
    public TachyonSchematicService(BlockChangePacketSender packetSender) {
        this.packetSender = packetSender;
    }

    @Override
    public CompletableFuture<Schematic> createSchematicAsync(Location start, Location end, Location origin) {
        return CompletableFuture.supplyAsync(() -> 
            new TachyonSchematic(start, end, origin, packetSender)
        );
    }

    @Override
    public CompletableFuture<Schematic> loadSchematicAsync(File file) {
        // TODO: Implement file loading
        throw new UnsupportedOperationException("Loading from file not yet implemented");
    }
}