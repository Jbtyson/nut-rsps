package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

import java.util.Arrays;
import java.util.List;

public class Title implements Command {

    private static final List<String> INAPPROPRIATE_TITLES = Arrays.asList("nigger", "ass", "boobs");

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts.length < 2) {
            player.getPacketSender().sendMessage("Usage: ::title <text>");
            return;
        }
        // Strip client control/colour codes (e.g. @red@, @cr1@) and restrict to a
        // safe character set to prevent staff-crown/colour impersonation.
        String sanitized = parts[1].replaceAll("@[^@]*@", "").replaceAll("[^A-Za-z0-9 ]", "").trim();
        if (sanitized.isEmpty()) {
            player.getPacketSender().sendMessage("That title is not allowed.");
            return;
        }
        if (sanitized.length() > 20) {
            sanitized = sanitized.substring(0, 20);
        }
        final String title = sanitized;
        if (INAPPROPRIATE_TITLES.stream().anyMatch(t -> title.toLowerCase().contains(t))) {
            player.getPacketSender().sendMessage("You're not allowed to have that in your title.");
            return;
        }
        player.setLoyaltyTitle("@blu@" + title);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
