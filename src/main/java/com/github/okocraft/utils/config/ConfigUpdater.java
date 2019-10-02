package com.github.okocraft.utils.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.okocraft.utils.Utils;

public final class ConfigUpdater {

    private static Utils plugin = Utils.getInstance();

    private ConfigUpdater() {
    }

    /**
     * Version sintax: majour.minor.patch
     * <p>
     * Change which is incompatible to previous version -> majour
     * <p>
     * Change which is compatible to previous version -> minor
     * <p>
     * Bug fix which is compatible to previous version -> patch
     * <p>
     */

    /**
     * Updates old config. If config is incompatible because of majour update, it
     * will be copied to {@code old} folder.
     */
    public static void update() {
        String version = plugin.getDescription().getVersion();
        String oldVersion = Config.getConfigVersion();

        if (version.equalsIgnoreCase(oldVersion)) {
            return;
        }

        if (!oldVersion.startsWith(String.valueOf(version.charAt(0)))) {
            Path datafoler = plugin.getDataFolder().toPath();
            Path oldDirectory = datafoler.resolve("old").resolve(oldVersion);
            try {
                Files.createDirectories(oldDirectory);
                for (File file : datafoler.toFile().listFiles()) {
                    if (file.getName().equals("old")) {
                        continue;
                    }
                    Path dest = oldDirectory.resolve(file.getName());
                    copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    delete(file.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            plugin.getLogger()
                    .info("Current config files might be incompatible to this version. Please configure again.");
            return;
        }
        
        Config.saveAllDefaultConfigs();

        if (version.compareTo(oldVersion) > 0) {
            plugin.getLogger().info("Updating...");

            if (oldVersion.equals("2.0.0")) {
            }

        }
    }

    private static void copy(Path file, Path dest, StandardCopyOption option) {
        try {
            if (Files.isDirectory(file)) {
                Files.createDirectories(dest);
                for (File subFile : file.toFile().listFiles()) {
                    copy(subFile.toPath(), dest.resolve(subFile.getName()), option);
                }
            } else {
                Files.copy(file, dest, option);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delete(Path file) {
        try {
            if (Files.isDirectory(file)) {
                for (File subFile : file.toFile().listFiles()) {
                    delete(subFile.toPath());
                }
            }

            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}