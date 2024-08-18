package me.athish.tachyon;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import me.athish.tachyon.block.BlockChanger;
import me.athish.tachyon.serialization.SerializableLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Schematic implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_EXTENSION = ".tachyon";
    private static final Kryo kryo = new Kryo();

    private Map<SerializableLocation, Material> blocks = new HashMap<>();
    private SerializableLocation origin;

    static {
        kryo.register(Schematic.class);
        kryo.register(HashMap.class);
        kryo.register(SerializableLocation.class);
        kryo.register(Material.class);
    }

    private Schematic() {
    }

    private Schematic(Location start, Location end, Location origin) {
        copyBlocks(start, end, origin);
    }

    private Schematic(File file) throws IOException {
        load(file);
    }

    private void copyBlocks(Location start, Location end, Location origin) {
        World world = start.getWorld();
        int minX = Math.min(start.getBlockX(), end.getBlockX());
        int minY = Math.min(start.getBlockY(), end.getBlockY());
        int minZ = Math.min(start.getBlockZ(), end.getBlockZ());
        int maxX = Math.max(start.getBlockX(), end.getBlockX());
        int maxY = Math.max(start.getBlockY(), end.getBlockY());
        int maxZ = Math.max(start.getBlockZ(), end.getBlockZ());

        this.origin = new SerializableLocation(origin);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    blocks.put(new SerializableLocation(loc), world.getBlockAt(loc).getType());
                }
            }
        }
    }

    public void pasteSync(Location pasteLocation, boolean ignoreAir) {
        Location originLoc = origin.toLocation();
        for (Map.Entry<SerializableLocation, Material> entry : blocks.entrySet()) {
            if (ignoreAir && entry.getValue() == Material.AIR) continue;

            Location loc = entry.getKey().toLocation();
            loc.subtract(originLoc);
            loc.add(pasteLocation);
            BlockChanger.setSectionBlock(loc, entry.getValue());
        }
    }

    public CompletableFuture<Void> pasteAsync(Location pasteLocation, boolean ignoreAir) {
        return CompletableFuture.runAsync(() -> {
            Location originLoc = origin.toLocation();
            for (Map.Entry<SerializableLocation, Material> entry : blocks.entrySet()) {
                if (ignoreAir && entry.getValue() == Material.AIR) continue;

                Location loc = entry.getKey().toLocation();
                loc.subtract(originLoc);
                loc.add(pasteLocation);
                BlockChanger.setSectionBlockAsynchronously(loc, new ItemStack(entry.getValue()), false);
            }
        });
    }

    public void save(File file) throws IOException {
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, this);
        }
    }

    public CompletableFuture<Void> saveAsync(File file) {
        return CompletableFuture.runAsync(() -> {
            try {
                save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void load(File file) throws IOException {
        try (Input input = new Input(new FileInputStream(file))) {
            Schematic loadedSchematic = kryo.readObject(input, Schematic.class);
            this.blocks = loadedSchematic.blocks;
            this.origin = loadedSchematic.origin;
        }
    }

    public CompletableFuture<Void> loadAsync(File file) {
        return CompletableFuture.runAsync(() -> {
            try {
                load(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Schematic create(Location start, Location end, Location origin) {
        return new Schematic(start, end, origin);
    }

    public static CompletableFuture<Schematic> createAsync(Location start, Location end, Location origin) {
        return CompletableFuture.supplyAsync(() -> new Schematic(start, end, origin));
    }

    public static Schematic create(File file) throws IOException {
        return new Schematic(file);
    }

    public static CompletableFuture<Schematic> createAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new Schematic(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static String getFileExtension() {
        return FILE_EXTENSION;
    }
}