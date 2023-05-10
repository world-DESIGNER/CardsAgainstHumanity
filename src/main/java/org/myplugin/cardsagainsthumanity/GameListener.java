package org.myplugin.cardsagainsthumanity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.myplugin.cardsagainsthumanity.GameState;
public class GameListener implements Listener {

    private CardsAgainstHumanity plugin;

    public GameListener(CardsAgainstHumanity plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (plugin.isPlayer(uuid) && plugin.getGameState() == GameState.PLAYING && !plugin.isCzar(uuid)) {
            event.setCancelled(true);
            PlayerData pData = plugin.getPlayerData(uuid);
            String[] whiteCardNumbers = event.getMessage().split(",");
            List<String> chosenCards = new ArrayList<>();
            for (String num : whiteCardNumbers) {
                int index = Integer.parseInt(num.trim()) - 1;
                chosenCards.add(pData.getHand().get(index));
            }
            plugin.getSubmittedCardsOwners().put(pData.getSubmittedCardIndex(), uuid);
        }
    }
}