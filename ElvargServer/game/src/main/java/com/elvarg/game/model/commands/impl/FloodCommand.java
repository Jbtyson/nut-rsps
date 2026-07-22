package com.elvarg.game.model.commands.impl;

import com.elvarg.Server;
import com.elvarg.game.GameConstants;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class FloodCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts.length < 2) {
            player.getPacketSender().sendMessage("Usage: ::flood <amount>");
            return;
        }
        int amt;
        try {
            amt = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            player.getPacketSender().sendMessage("Usage: ::flood <amount>");
            return;
        }
        Server.getFlooder().login(amt);
    }

    @Override
    public boolean canUse(Player player) {
        if (!GameConstants.DEV_TOOLS_ENABLED) {
            return false;
        }
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
