package org.myplugin.cardsagainsthumanity;
import java.util.ArrayList;
import java.util.List;
import org.myplugin.cardsagainsthumanity.GameState;
public class PlayerData {
    private int score;
    private int submittedCardIndex;
    private List<String> hand;

    public PlayerData() {
        this.score = 0;
        this.hand = new ArrayList<>();
    }

    public int getScore() {
        return score;
    }
    public int getSubmittedCardIndex() {
        return submittedCardIndex;
    }

    public void setSubmittedCardIndex(int submittedCardIndex) {
        this.submittedCardIndex = submittedCardIndex;
    }

    public void incrementScore() {
        this.score++;
    }

    public List<String> getHand() {
        return hand;
    }

    public void removeFromHand(String card) {
        hand.remove(card);
    }
}