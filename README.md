# Tachyon Refreshed
[![](https://jitpack.io/v/Reddishye/TachyonRefreshed.svg)](https://jitpack.io/#Reddishye/TachyonRefreshed)

A high-performance, cross-version schematic library for Minecraft servers that provides an efficient alternative to WorldEdit for handling schematics.

## 🌟 Key Features

### Performance & Efficiency
- **Zero TPS Impact**: Utilizes PacketEvents for block placement, bypassing Bukkit methods
- **Fully Asynchronous Operations**: All operations (create/save/load/paste) run asynchronously
- **Lightning-Fast Performance**: 
  - Up to 10x faster than traditional Java serialization for loading/saving
  - Faster block placement compared to WorldEdit
  - Optimized for large schematics

### Compatibility & Integration
- **Cross-Version Support**: Compatible with Minecraft versions 1.19 through 1.20
- **Lightweight**: Minimal dependencies, significantly smaller than WorldEdit
- **Custom File Format**: Uses `.tachyon` extension (customizable)
- **Smart PacketEvents Integration**: Automatically uses existing PacketEvents instance if available

## 📦 Installation

Add this to your `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'com.gradleup.shadow' version '9.0.0-beta4'  // Latest version of GradleUp Shadow
}

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Reddishye:TachyonRefreshed:VERSION'
    // Optional: If you want to use your own PacketEvents instance
    implementation 'com.github.retrooper.packetevents:spigot:2.7.0'
}

shadowJar {
    // Relocate Tachyon and all its dependencies to your package
    relocate 'com.github.Reddishye.tachyonRefreshed', 'your.package.lib.tachyon'
    // If using your own PacketEvents, relocate it too
    relocate 'io.github.retrooper.packetevents', 'your.package.lib.packetevents'
}
```

That's it! All dependencies (Guice, etc.) are included and relocated automatically. If you have PacketEvents installed as a plugin or already included in your project, Tachyon will use that instance instead of creating its own.

## 🚀 Quick Start Guide

### Initialize the Library

First, initialize TachyonLibrary in your plugin's onEnable:

```java
public class YourPlugin extends JavaPlugin {
    private TachyonLibrary tachyon;
    
    @Override
    public void onEnable() {
        // Initialize Tachyon with your plugin instance
        // It will automatically detect and use any existing PacketEvents instance
        tachyon = new TachyonLibrary(this);
    }
    
    @Override
    public void onDisable() {
        if (tachyon != null) {
            tachyon.disable();
        }
    }
}
```

### Basic Usage

1. **Creating a Schematic**
```java
// From locations
Location pos1 = /* first corner */;
Location pos2 = /* second corner */;
Location origin = /* origin point */;
Schematic.createAsync(pos1, pos2, origin).thenAccept(schematic -> {
    // Schematic created successfully
});

// From file
File file = new File("plugins/YourPlugin/schematics/house.tachyon");
Schematic.createAsync(file).thenAccept(schematic -> {
    // Schematic loaded successfully
});
```

2. **Saving a Schematic**
```java
File saveFile = new File("plugins/YourPlugin/schematics/build.tachyon");
schematic.saveAsync(saveFile).thenRun(() -> {
    // Schematic saved successfully
});
```

3. **Pasting a Schematic**
```java
Location pasteLocation = /* where to paste */;
schematic.pasteAsync(pasteLocation, true) // true = ignore air blocks
    .thenRun(() -> {
        // Paste completed successfully
    });
```

### Advanced Operations

1. **Schematic Manipulation**
```java
// Rotate the schematic
schematic.rotate(90);  // Rotates 90 degrees clockwise
schematic.rotate(180); // Rotates 180 degrees
schematic.rotate(270); // Rotates 270 degrees clockwise (90 degrees counterclockwise)

// Flip the schematic
schematic.flip("up");    // Flips upward
schematic.flip("down");  // Flips downward
schematic.flip("left");  // Flips left
schematic.flip("right"); // Flips right
schematic.flip("north"); // Flips north
schematic.flip("south"); // Flips south

// Get block count
int totalBlocks = schematic.getBlockCount();
```

## 🔧 Example Plugin

Here's a complete example plugin that demonstrates all features:

```java
public class ExamplePlugin extends JavaPlugin {
    private TachyonLibrary tachyon;
    private final Map<String, Schematic> schematics = new HashMap<>();
    
    @Override
    public void onEnable() {
        tachyon = new TachyonLibrary(this);
        
        // Register commands
        getCommand("tschematic").setExecutor(new SchematicCommand());
    }
    
    @Override
    public void onDisable() {
        if (tachyon != null) {
            tachyon.disable();
        }
    }
    
    private class SchematicCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players");
                return true;
            }
            
            Player player = (Player) sender;
            
            if (args.length < 1) {
                player.sendMessage("Usage: /tschematic <create|save|load|paste|rotate|flip>");
                return true;
            }
            
            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        player.sendMessage("Usage: /tschematic create <name>");
                        return true;
                    }
                    
                    // Get WorldEdit selection
                    Selection sel = WorldEdit.getInstance().getSelection(player);
                    if (sel == null) {
                        player.sendMessage("Please make a selection first");
                        return true;
                    }
                    
                    Location pos1 = new Location(player.getWorld(), sel.getMinimumPoint().getX(),
                        sel.getMinimumPoint().getY(), sel.getMinimumPoint().getZ());
                    Location pos2 = new Location(player.getWorld(), sel.getMaximumPoint().getX(),
                        sel.getMaximumPoint().getY(), sel.getMaximumPoint().getZ());
                    Location origin = player.getLocation();
                    
                    Schematic.createAsync(pos1, pos2, origin).thenAccept(schematic -> {
                        schematics.put(args[1], schematic);
                        player.sendMessage("Schematic created: " + args[1]);
                    });
                    break;
                    
                case "save":
                    if (args.length < 2) {
                        player.sendMessage("Usage: /tschematic save <name>");
                        return true;
                    }
                    
                    Schematic saveSchematic = schematics.get(args[1]);
                    if (saveSchematic == null) {
                        player.sendMessage("Schematic not found: " + args[1]);
                        return true;
                    }
                    
                    File saveFile = new File(getDataFolder(), args[1] + ".tachyon");
                    saveSchematic.saveAsync(saveFile).thenRun(() ->
                        player.sendMessage("Schematic saved: " + args[1])
                    );
                    break;
                    
                case "load":
                    if (args.length < 2) {
                        player.sendMessage("Usage: /tschematic load <name>");
                        return true;
                    }
                    
                    File loadFile = new File(getDataFolder(), args[1] + ".tachyon");
                    if (!loadFile.exists()) {
                        player.sendMessage("Schematic file not found: " + args[1]);
                        return true;
                    }
                    
                    Schematic.createAsync(loadFile).thenAccept(schematic -> {
                        schematics.put(args[1], schematic);
                        player.sendMessage("Schematic loaded: " + args[1]);
                    });
                    break;
                    
                case "paste":
                    if (args.length < 2) {
                        player.sendMessage("Usage: /tschematic paste <name>");
                        return true;
                    }
                    
                    Schematic pasteSchematic = schematics.get(args[1]);
                    if (pasteSchematic == null) {
                        player.sendMessage("Schematic not found: " + args[1]);
                        return true;
                    }
                    
                    pasteSchematic.pasteAsync(player.getLocation(), true).thenRun(() ->
                        player.sendMessage("Schematic pasted: " + args[1])
                    );
                    break;
                    
                case "rotate":
                    if (args.length < 3) {
                        player.sendMessage("Usage: /tschematic rotate <name> <angle>");
                        return true;
                    }
                    
                    Schematic rotateSchematic = schematics.get(args[1]);
                    if (rotateSchematic == null) {
                        player.sendMessage("Schematic not found: " + args[1]);
                        return true;
                    }
                    
                    try {
                        double angle = Double.parseDouble(args[2]);
                        rotateSchematic.rotate(angle);
                        player.sendMessage("Schematic rotated: " + args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid angle: " + args[2]);
                    }
                    break;
                    
                case "flip":
                    if (args.length < 3) {
                        player.sendMessage("Usage: /tschematic flip <name> <direction>");
                        return true;
                    }
                    
                    Schematic flipSchematic = schematics.get(args[1]);
                    if (flipSchematic == null) {
                        player.sendMessage("Schematic not found: " + args[1]);
                        return true;
                    }
                    
                    try {
                        flipSchematic.flip(args[2]);
                        player.sendMessage("Schematic flipped: " + args[1]);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("Invalid direction: " + args[2]);
                    }
                    break;
                    
                default:
                    player.sendMessage("Unknown command: " + args[0]);
                    break;
            }
            
            return true;
        }
    }
}
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
