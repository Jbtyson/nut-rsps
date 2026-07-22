package com.elvarg.game.entity.impl.player.persistence.jsonfile;

import com.elvarg.Server;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.persistence.PlayerPersistence;
import com.elvarg.game.entity.impl.player.persistence.PlayerSave;
import com.elvarg.util.Misc;
import com.elvarg.util.PasswordUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.logging.Level;

public class JSONFilePlayerPersistence extends PlayerPersistence {

    private static final String PATH = "../data/saves/characters/";
    private static final Gson BUILDER = new GsonBuilder().create();

    @Override
    public PlayerSave load(String username) {
        File file = resolveSaveFile(username);
        if (file == null || !file.exists()) {
            return null;
        }

        try (FileReader fileReader = new FileReader(file)) {
            return BUILDER.fromJson(fileReader, PlayerSave.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Player player) {
        PlayerSave save = PlayerSave.fromPlayer(player);

        File file = resolveSaveFile(player.getUsername());
        if (file == null) {
            Server.getLogger().log(Level.SEVERE, "Refusing to save character with unsafe username: " + player.getUsername());
            return;
        }
        setupDirectory(file);

        Gson builder = new GsonBuilder().setPrettyPrinting().create();

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(builder.toJson(save));
		} catch (Exception e) {
			Server.getLogger().log(Level.SEVERE, "An error has occurred while saving a character file!", e);
            throw new RuntimeException(e);
		}
    }

    @Override
    public boolean exists(String username) {
        File file = resolveSaveFile(username);
        return file != null && file.exists();
    }

    /**
     * Resolves the on-disk save file for a username while preventing path
     * traversal. The username is normalised and the resolved path is confirmed
     * to stay within {@link #PATH}; any attempt to escape the directory (e.g.
     * "../", absolute paths, path separators) yields {@code null}.
     */
    private static File resolveSaveFile(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        String formatted = Misc.formatPlayerName(username.toLowerCase());
        Path base = Paths.get(PATH).toAbsolutePath().normalize();
        Path resolved = base.resolve(formatted + ".json").normalize();
        if (!resolved.startsWith(base)) {
            return null;
        }
        return resolved.toFile();
    }

    @Override
    public boolean checkPassword(String plainPassword, PlayerSave playerSave) {
        String stored = playerSave.getPasswordHashWithSalt();
        if (stored == null || plainPassword == null) {
            return false;
        }
        if (PasswordUtil.isHashed(stored)) {
            return PasswordUtil.passwordsMatch(plainPassword, stored);
        }
        // Legacy plaintext record (created before hashing). Compare in constant
        // time; the login flow re-hashes the password on successful login.
        return MessageDigest.isEqual(plainPassword.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }

    private void setupDirectory(File file) {
        file.getParentFile().setWritable(true);
        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
            } catch (SecurityException e) {
                System.out.println("Unable to create directory for player data!");
                throw new RuntimeException(e);
            }
        }
    }
}
