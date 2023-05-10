package org.myplugin.cardsagainsthumanity;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CardsAgainstHumanity extends JavaPlugin {

    public Map<UUID, Boolean> getPlayers() {
        return players;
    }

    private List<String> whiteCards;
    private List<String> blackCards;
    private Map<UUID, Boolean> players;
    private UUID currentCzar;
    private GameState gameState;
    private Map<UUID, PlayerData> playerData;
    private Map<Integer, UUID> submittedCardsOwners;
    public UUID getNextPlayer(UUID currentUUID) {
        List<UUID> playerUUIDs = new ArrayList<>(players.keySet());
        int index = playerUUIDs.indexOf(currentUUID);

        // If the current player is the last one in the list, return the first player's UUID
        if (index == playerUUIDs.size() - 1) {
            return playerUUIDs.get(0);
        }

        // Otherwise, return the next player's UUID
        return playerUUIDs.get(index + 1);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getCommand("cah").setExecutor(new GameCommand(this));

        whiteCards = loadCards("Cards/WhiteCards.txt");
        blackCards = loadCards("Cards/BlackCards.txt");
        players = new HashMap<>();
        gameState = GameState.LOBBY;
        playerData = new HashMap<>();
        submittedCardsOwners = new HashMap<>();
    }
    public Map<Integer, UUID> getSubmittedCardsOwners() {
        return submittedCardsOwners;
    }

    public void setSubmittedCardsOwners(Map<Integer, UUID> submittedCardsOwners) {
        this.submittedCardsOwners = submittedCardsOwners;
    }


    public List<String> loadCards(String path) {
        List<String> cards = new ArrayList<>();
        File file = new File(getDataFolder(), path);

        if (!file.exists()) {
            saveResource(path, false);
        }

        try {
            cards = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cards;
    }
    private int roundTimerTaskId;

    public void startGame() {
        gameState = GameState.PLAYING;
        currentCzar = getCzar();
        Player czarPlayer = Bukkit.getPlayer(currentCzar);
        if (czarPlayer != null) {
            String blackCard = getRandomCard(blackCards);
            broadcast(ChatColor.GOLD + "The Card Czar is: " + czarPlayer.getDisplayName());
            broadcast(ChatColor.DARK_PURPLE + "Black Card: " + ChatColor.WHITE + blackCard);
            dealWhiteCards();
            roundTimerTaskId = startRoundTimer(60);
        } else {
            // Handle the case where the czar player is null
            broadcast(ChatColor.RED + "Error: The Card Czar could not be found.");
        }
    }


    public int startRoundTimer(int seconds) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (gameState == GameState.PLAYING) {
                Bukkit.getScheduler().cancelTask(roundTimerTaskId);
                broadcast(ChatColor.RED + "Time's up! The Card Czar must choose a winner.");
            }
        }, seconds * 20L);
    }

    public void dealWhiteCards() {
        for (UUID uuid : players.keySet()) {
            if (!players.get(uuid)) {
                PlayerData pData = playerData.get(uuid);
                while (pData.getHand().size() < 7) {
                    String whiteCard = getRandomCard(whiteCards);
                    pData.getHand().add(whiteCard);
                }
                Player p = Bukkit.getPlayer(uuid);
                p.sendMessage(ChatColor.GREEN + "Your White Cards: " + ChatColor.WHITE + String.join(", ", pData.getHand()));
            }
        }
    }
    public void startNextRound() {
        rotateCzar();
        dealWhiteCards();
        String blackCard = getNextBlackCard();
        broadcast(ChatColor.GOLD + "The Card Czar is: " + Bukkit.getPlayer(currentCzar).getDisplayName());
        broadcast(ChatColor.DARK_PURPLE + "Black Card: " + ChatColor.WHITE + blackCard);
    }

    public void rotateCzar() {
        List<UUID> nonCzars = new ArrayList<>();
        for (Map.Entry<UUID, Boolean> entry : players.entrySet()) {
            if (!entry.getValue()) {
                nonCzars.add(entry.getKey());
            }
        }

        if (nonCzars.size() > 0) {
            players.put(currentCzar, false);
            currentCzar = nonCzars.get(new Random().nextInt(nonCzars.size()));
            players.put(currentCzar, true);
        }
    }

    public String getNextBlackCard() {
        if (blackCards.size() == 0) {
            blackCards = loadCards("Cards/BlackCards.txt");
        }
        String blackCard = getRandomCard(blackCards);
        blackCards.remove(blackCard);
        return blackCard;
    }

    public void refillWhiteCardsIfNeeded() {
        if (whiteCards.size() < players.size() * 7) {
            whiteCards = loadCards("Cards/WhiteCards.txt");
        }
    }
    public void broadcast(String message) {
        for (UUID player : players.keySet()) {
            Bukkit.getPlayer(player).sendMessage(message);
        }
    }

    public String getRandomCard(List<String> cards) {
        Collections.shuffle(cards);
        return cards.get(0);
    }

    public UUID getCzar() {
        UUID czar = null;
        for (Map.Entry<UUID, Boolean> entry : players.entrySet()) {
            if (entry.getValue()) {
                czar = entry.getKey();
                break;
            }
        }
        return czar;
    }

    public void setPlayer(UUID uuid, boolean czar) {
        players.put(uuid, czar);
        playerData.put(uuid, new PlayerData());
    }

    public void removePlayer(UUID uuid) {
        if (players.containsKey(uuid)) {
            if (isCzar(uuid)) {
                if (players.size() > 1) {
                    UUID nextCzar = getNextPlayer(uuid);
                    setPlayer(nextCzar, true);
                }
            }
            players.remove(uuid);
        }
    }


    public boolean isPlayer(UUID uuid) {
        return players.containsKey(uuid);
    }

    public boolean isCzar(UUID uuid) {
        return players.get(uuid) != null && players.get(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
