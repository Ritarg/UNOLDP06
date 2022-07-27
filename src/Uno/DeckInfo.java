package Uno;

import java.io.Serializable;
import java.util.List;

public class DeckInfo implements Serializable {

    private final List<IndividualCardView> deck;


    public DeckInfo(final List<IndividualCardView> deck) {

        this.deck = deck;
    }

    public List<IndividualCardView> getDeck() {

        return deck;
    }

    @Override
    public String toString() {

        return "[DECK INFO]" +
                "\n------------------\n" +
                "[Deck][" + deck.size() + "]" + deck + "\n" +
                "------------------\n";
    }

}
