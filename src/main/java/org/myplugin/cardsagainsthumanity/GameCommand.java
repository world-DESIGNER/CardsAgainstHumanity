package org.myplugin.cardsagainsthumanity;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.myplugin.cardsagainsthumanity.GameState;
import java.util.UUID;
public class GameCommand implements CommandExecutor {

    private CardsAgainstHumanity plugin;

    public GameCommand(CardsAgainstHumanity plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /cah <join|leave|start|choose|scores>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        if (!subcommand.equals("join") && !subcommand.equals("leave") && !subcommand.equals("choose") && !subcommand.equals("scores")) {
            if (!player.hasPermission("cardsagainsthumanity.command.cah.op")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
        } else {
            if (!player.hasPermission("cardsagainsthumanity.command.cah.basic")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
        }

        switch (subcommand) {
            case "join":
                if (plugin.getPlayers().size() >= 6) {
                    player.sendMessage(ChatColor.RED + "The game is full. You cannot join.");
                } else {
                    plugin.setPlayer(uuid, false);
                    player.sendMessage(ChatColor.GREEN + "You have joined the game!");
                }
                break;
            case "leave":
                plugin.removePlayer(uuid);
                player.sendMessage(ChatColor.GREEN + "You have left the game!");
                break;
            case "start":
                if (plugin.getGameState() == GameState.LOBBY) {
                    plugin.startGame();
                } else {
                    player.sendMessage(ChatColor.RED + "The game has already started!");
                }
                break;
            case "choose":
                if (plugin.isCzar(uuid)) {
                    int winningIndex = Integer.parseInt(args[1]) - 1;
                    UUID winner = plugin.getSubmittedCardsOwners().get(winningIndex);
                    PlayerData winnerData = plugin.getPlayerData(winner);
                    winnerData.incrementScore();
                    plugin.broadcast(ChatColor.GOLD + Bukkit.getPlayer(winner).getDisplayName() + " has won this round!");
                    plugin.startNextRound();
                } else {
                    player.sendMessage(ChatColor.RED + "Only the Card Czar can choose the winning card.");
                }
                break;
            case "scores":
                for (UUID playerUUID : plugin.getPlayers().keySet()) {
                    Player p = Bukkit.getPlayer(playerUUID);
                    int score = plugin.getPlayerData(playerUUID).getScore();
                    player.sendMessage(ChatColor.GREEN + p.getDisplayName() + ": " + score);
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Invalid command. Usage: /cah <join|leave|start|choose|scores>");
        }

        return true;
    }
}

