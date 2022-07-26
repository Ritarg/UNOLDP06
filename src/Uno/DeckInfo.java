package Uno;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

public class DeckInfo implements Serializable {

    private final List<IndividualCardView> deck;
    private final Stack<IndividualCardView> drawPile;

    public DeckInfo(final List<IndividualCardView> deck, final Stack<IndividualCardView> drawPile) {

        this.deck = deck;
        this.drawPile = drawPile;
    }

    public List<IndividualCardView> getDeck() {

        return deck;
    }

    public Stack<IndividualCardView> getDrawPile() {

        return drawPile;
    }

    @Override
    public String toString() {

        return "[DECK INFO]" +
                "\n------------------\n" +
                "[Deck][" + deck.size() + "]" + deck + "\n" +
                "[DrawPile][" + drawPile.size() + "]" + drawPile + "\n" +
                "------------------\n";
    }

}
