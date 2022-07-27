package Uno;

import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class Player {
    //Contém a Lista das Cartas que se encontra na classe IndividualCardView
    private final ArrayList<IndividualCardView> cards;
    //Contém a Lista das Imagem das Cartas que se encontra na classe cardsView
    private final ArrayList<ImageView> cardsView;
    private String name;
    private boolean myTurn = false;

    public Player() {

        this.cards = new ArrayList<>();
        this.cardsView = new ArrayList<>();
    }

    //Método que desenha
    public void draw(final IndividualCardView drawnCard) {

        cards.add(drawnCard);
    }

    //Vai buscar as cartas do jogador
    public ArrayList<IndividualCardView> getCards() {

        return this.cards;
    }

    public ArrayList<ImageView> getCardsView() {

        return this.cardsView;
    }

    //método que Remove as cartas da Classe IndividualCardView
    public IndividualCardView remove(final IndividualCardView removedCard) {

        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(removedCard)) {
                return cards.remove(i);
            }
        }

        // se eles não tiverem a carta
        return cards.get(0);
    }

    public void setName(final String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public boolean isMyTurn() {

        return myTurn;
    }

    public void setMyTurn(final boolean myTurn) {

        this.myTurn = myTurn;
    }
}

