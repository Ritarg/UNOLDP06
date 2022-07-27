package Uno;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Classe auxiliar à classe {@link FXMLController}.
 * <p> Contem os principais metodos despoletados pelos eventos.
 */
public class GameControls {
    private int index;
    private ControlUNO control;
    private boolean isWild;
    private boolean isGameWon = false;
    private DeckInfo myDeck;
    private DeckInfo opponentDeck;
    private DataOutputStream dos;
    private ObjectOutputStream objectOut;
    private Stack<IndividualCardView> drawPile;
    private Stack<IndividualCardView> discardPile;

    /**
     * Default constructor
     */
    public GameControls() {
    }

    /**
     * Constructor
     */
    public GameControls(final DataOutputStream dos, final ObjectOutputStream objOut) {

        this.dos = dos;
        this.objectOut = objOut;
        this.control = new ControlUNO();
        this.drawPile = control.cards().getDrawPile();
        this.discardPile = control.cards().getDiscardPile();
    }

    //Botão inicial, Imagem/Icon do UNO que ao carregar inicia o Jogo
    public void handleStartButton() throws IOException {

        Platform.runLater(() -> {
            // controla visibilidade dos elementos iniciais
            final String name = FXMLController.nameInStatic.getText();
            FXMLController.nameOut1Static.setText(name);
            FXMLController.nameOut1Static.setVisible(false);
            FXMLController.nameInStatic.setVisible(false);
            FXMLController.text_unoStatic.setVisible(false);
            setVisibilty(false);
            // remove a imagem do Logo
            FXMLController.anchorStatic.getChildren().remove(FXMLController.startStatic);
            // set player name
            this.control.getPlayer().setName(name);
            try {
                writeString("#nome-" + this.control.getPlayer().getName());
                // As cartas são baralhadas para dar aos jogadores
                reshuffle();
                // distribui 7 cartas
                writeObject(deal());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private DeckInfo deal() {

        // da 7 cartas
        for (int i = 0; i < 7; i++) {
            control.getPlayer().draw(drawPile.pop());
            FXMLController.drawPileStatic.getChildren().remove(FXMLController.drawPileStatic.getChildren().size() - 1);
        }
        logDecks();

        return myDeck = new DeckInfo(control.getPlayer().getCards());
    }

    public void initGame(final boolean myTurn, final boolean isFirstMoveDone) throws IOException {

        control.getPlayer().setMyTurn(myTurn);

        Platform.runLater(() -> {
            // desenha cartas do jogador 1
            for (IndividualCardView card : myDeck.getDeck()) {
                FXMLController.playerOneHBoxStatic.getChildren().add(new ImageView(card.getIcon()));
            }
            // desenha cartas do oponente
            System.out.println("Deck adversario:\n" + opponentDeck.getDeck());
            for (IndividualCardView card : opponentDeck.getDeck()) {
                FXMLController.playerTwoHBoxStatic.getChildren().add(new ImageView(card.getBackIcon()));
            }
            // a primeira "jogada" é a carta que sai da drawPile para a discardPile, para dar início ao jogo
            // so pode ocorrer uma vez
            if (!isFirstMoveDone) {
                final IndividualCardView firstCard = drawPile.pop();
                discardPile.add(firstCard);
                logDecks();
                // define a carta atual
                setCurrentCard();
                // atualiza drawPileStatic e discardPileStatic
                final int i = FXMLController.drawPileStatic.getChildren().size() - 1;
                FXMLController.drawPileStatic.getChildren().remove(i);
                FXMLController.discardPileStatic.getChildren().add(new ImageView(firstCard.getIcon()));
                try {
                    writeString("#card-drawn-" + i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // apenas desenha
                setVisibilty(false);
            }
        });
    }

    /* Método Draw - Jogadores fazem uma jogada
     * txtField - Campo de texto com comentação dos passos de cada jogador
     * As cartas recebidas vêm da pilha de cartas do StackPane com o fx:id="drawPile"
     * As cartas descartadas vão para a pilha de cartas do StackPane com o fx:id="discardPile"
     * playerOneHBox - container onde se encontram exibidas as cartas do Jogador 1
     * playerTwoHBox - container onde se encontram exibidas as cartas do Jogador 2*/
    public void drawCard(final boolean myTurn) throws IOException {

        Platform.runLater(() -> {
            // remove da drawPile
            FXMLController.drawPileStatic.getChildren().remove(FXMLController.drawPileStatic.getChildren().size() - 1);
            // se é a minha vez, desenha carta mostra para cima, senao mostra carta virada para baixo no campo do adversario
            if (myTurn) {
                ImageView temp = new ImageView(drawPile.peek().getIcon());
                FXMLController.playerOneHBoxStatic.getChildren().add(temp);
            } else {
                ImageView temp = new ImageView(drawPile.peek().getBackIcon());
                FXMLController.playerTwoHBoxStatic.getChildren().add(temp);
            }
            control.getPlayer().draw(drawPile.pop());
            FXMLController.txtFieldStatic.setText("\nJogaste uma carta, é a vez do adversario");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        });
        // carta retirada, muda a vez
        control.getPlayer().setMyTurn(false);
        // envia msg ao outro jogador
        writeString("#card-drawn-" + control.getPlayer());
    }


    // Acao de jogar uma carta. Adiciona a discardPile
    public void discard(final int cardIndex) {

        // adiciona a discardPile
        discardPile.push(control.getPlayer().remove(control.getPlayer().getCards().get(cardIndex)));
        Platform.runLater(() -> {
            if (this.control.getPlayer().isMyTurn()) {
                FXMLController.discardPileStatic.getChildren().add(FXMLController.playerOneHBoxStatic.getChildren().remove(cardIndex));
            } else {
                FXMLController.discardPileStatic.getChildren().add(FXMLController.playerTwoHBoxStatic.getChildren().remove(cardIndex));
            }
        });
    }

    public void select() {

        if (!isWild) {
            Platform.runLater(() -> {
                ArrayList<ImageView> arr = new ArrayList<>();
                for (int i = 0; i < FXMLController.playerOneHBoxStatic.getChildren().size(); i++) {
                    arr.add((ImageView) FXMLController.playerOneHBoxStatic.getChildren().get(i));
                }
                for (int i = 0; i < FXMLController.playerOneHBoxStatic.getChildren().size(); i++) {
                    arr.get(i).setOnMouseClicked(this::handle);
                }
            });
        }
    }

    /**
     * Código que manuseia os movimentos do cursor/rato - mouseEvents
     * Representa as jogadas dos jogadores
     * Ação de quando o Jogador clica numa carta
     * Ciclo de quando uma carta Joker +4 é jogada, o outro Jogador recebe 4 cartas
     */
    public void handle(final MouseEvent event) {

        if (!isGameWon) {
            // Identifica qual a carta selecionada
            Platform.runLater(() -> {
                for (int i = 0; i < 13; i++) {
                    if (FXMLController.playerOneHBoxStatic.getChildren().get(i) == event.getTarget()) {
                        index = i;
                        break;
                    }
                }
            });
            //Se o Jogador 1 jogar uma carta Joker+4 este deve selecionar a cor que deseja
            //O Jogador 2 recebe 4 cartas aleatoriamente tiradas do baralho
            String playerCard = control.getPlayer().getCards().get(index).getName();
            if (control.getPlayer().getCards().get(index).getName().equals("noneplusfour-1")) {
                discard(index);
                setCurrentCard();
                for (int i = 0; i < 4; i++) {
                    //playerTwoDraw();
                }
                Platform.runLater(() -> {
                    FXMLController.txtFieldStatic.setText("\nJogaste uma carta Joker+4, escolhe a cor");
                    FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
                    if (control.getPlayer().getCards().size() == 0) {
                        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");

                    }
                });

                isWild = true;
                isVisible(true);
                //control.setCurrentPlayer(control.getPlayerTwo());

                //Se o Jogador 1 jogar uma carta Joker este deve selecionar a cor que deseja
            } else if (control.getPlayer().getCards().get(index).getName().equals("nonewild-1")) {
                Platform.runLater(() -> {
                    FXMLController.txtFieldStatic.setText("\nJogaste uma carta Joker, escolhe a cor");
                    FXMLController.txtFieldStatic.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                });

                discard(index);
                setCurrentCard();
                if (control.getPlayer().getCards().size() == 0) {
                    Platform.runLater(() -> FXMLController.txtFieldStatic.setText("Ganhaste o Jogo"));
                    setGameWon(true);
                }
                isWild = true;
                isVisible(true);
                //control.setCurrentPlayer(control.getPlayerTwo());
                // Add visibility
            }
            //Se o Jogador 1 jogar uma carta +2 o Jogador 2 recebe 2 cartas aleatoriamente tiradas do baralho
            String substring = playerCard.substring(playerCard.length() - 9, playerCard.length() - 2);
            System.out.println(substring);
            if (substring.equals("plustwo") && control.matches(control.getPlayer().getCards().get(index))) {
                discardPile.push(control.getPlayer().getCards().get(index));
                discard(index);
                setCurrentCard();
                for (int i = 0; i < 2; i++) {
                    //playerTwoDraw();
                }
                /*Se o tamanho for igual a 0, significa que o Jogador não possui mais cartas
                 * ou seja, este ganha o jogo */
                if (control.getPlayer().getCards().size() == 0) {
                    Platform.runLater(() -> {
                        FXMLController.txtFieldStatic.setText("Ganhaste o Jogo");
                        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
                    });

                    setGameWon(true);
                }
                //control.setCurrentPlayer(control.getPlayerTwo());
            } else if (control.matches(control.getPlayer().getCards().get(index))) {
                discardPile.push(control.getPlayer().getCards().get(index));
                discard(index);
                setCurrentCard();
                if (control.getPlayer().getCards().size() == 0) {
                    Platform.runLater(() -> {
                        FXMLController.txtFieldStatic.setText("Ganhaste o Jogo");
                        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
                    });

                    setGameWon(true);
                }
                //control.setCurrentPlayer(control.getPlayerTwo());
            }
        }
    }

    /* Baralha as cartas */
    private void reshuffle() {

        control.cards().shuffleDrawPile();

        // remove todas as cartas da DiscardPile
        if (FXMLController.discardPileStatic.getChildren().size() > 0) {
            FXMLController.discardPileStatic.getChildren().clear();
        }
        for (IndividualCardView drawPile : drawPile) {
            FXMLController.drawPileStatic.getChildren().add(new ImageView(drawPile.getBackIcon()));
        }

        FXMLController.txtFieldStatic.setText("Shuffled the draw deck");
        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
    }

    //Seleciona a cor das cartas
    public void redClicked() {

        Platform.runLater(() -> {
            control.getCurrentCard().setColor("red");
            isWild = false;
            isVisible(false);
            FXMLController.txtFieldStatic.setText("\nA cor é vermelha");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        });
    }

    //Azul
    public void blueClicked() {

        Platform.runLater(() -> {
            control.getCurrentCard().setColor("blue");
            isWild = false;
            isVisible(false);

            FXMLController.txtFieldStatic.setText("\nA cor é azul");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: blue; -fx-font-size: 16px;");
        });
    }

    //Verde
    public void greenClicked() {

        Platform.runLater(() -> {
            control.getCurrentCard().setColor("green");
            isWild = false;
            isVisible(false);

            FXMLController.txtFieldStatic.setText("\nA cor é verde");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        });

    }

    //Amarelo
    public void yellowClicked() {

        Platform.runLater(() -> {
            control.getCurrentCard().setColor("yellow");
            isWild = false;
            isVisible(false);

            FXMLController.txtFieldStatic.setText("\nA cor é amarelo");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: yellow; -fx-font-size: 16px;");
        });

    }

    public void setGameWon(boolean gameWon) {

        isGameWon = gameWon;
    }

    public String getColorStyle(String color) {

        return ("			     -fx-background-radius: 5em;\n" +
                "	             -fx-min-width: 50px;\n" +
                "	             -fx-min-height: 50px;\n" +
                "	             -fx-max-width: 50px; \n" +
                "	             -fx-max-height: 50px;\n" +
                "	             -fx-background-color:" + color + ";\n" +
                "	             -fx-border-color: black;\n" +
                "	             -fx-border-radius: 30;\n" +
                "	             visibility: hidden;");
    }

    public void setVisibilty(final boolean visible) {

        Platform.runLater(() -> {
            FXMLController.txtFieldStatic.setVisible(visible);
            FXMLController.drawPileStatic.setVisible(visible);
            FXMLController.discardPileStatic.setVisible(visible);
            FXMLController.playerOneHBoxStatic.setVisible(visible);
            FXMLController.playerTwoHBoxStatic.setVisible(visible);
            FXMLController.drawButtonStatic.setVisible(visible);
            FXMLController.text_waitStatic.setVisible(!visible);
        });
    }

    //Quando uma carta Joker é jogada torna se visivel a seleção da cor para a carta da proxima jogada
    private void isVisible(boolean visible) {

        Platform.runLater(() -> {
            if (visible) {
                FXMLController.redStatic.setStyle(getColorStyle("red") + "visibility: visible;");
                FXMLController.blueStatic.setStyle(getColorStyle("blue") + "visibility: visible;");
                FXMLController.yellowStatic.setStyle(getColorStyle("yellow") + "visibility: visible;");
                FXMLController.greenStatic.setStyle(getColorStyle("green") + "visibility: visible;");
            } else {
                FXMLController.redStatic.setVisible(false);
                FXMLController.blueStatic.setVisible(false);
                FXMLController.greenStatic.setVisible(false);
                FXMLController.yellowStatic.setVisible(false);
            }
        });
    }

    public void logDecks() {

        System.out.println("[TABLE INFO]" + "\n------------------\n" + "[DrawPile][" + drawPile.size() + "]" + drawPile);
        System.out.println("[DiscardPile][" + discardPile.size() + "]" + discardPile + "\n------------------\n");
    }

    private void setCurrentCard() {

        control.setCurrentCard(discardPile.peek());
    }

    public void setOpponentDeck(final DeckInfo opponentDeck) {

        this.opponentDeck = opponentDeck;
    }

    public Stack<IndividualCardView> getDrawPile() {

        return drawPile;
    }

    public void setDrawPile(final Stack<IndividualCardView> drawPile) {

        this.drawPile = drawPile;
    }

    public Stack<IndividualCardView> getDiscardPile() {

        return discardPile;
    }

    public void setDiscardPile(final Stack<IndividualCardView> discardPile) {

        this.discardPile = discardPile;
    }

    private void writeString(final String msg) throws IOException {

        if (msg == null) {
            throw new RuntimeException("Cannot write null String to DataOutputStream.");
        }

        System.out.println("Mensagem enviada: " + msg);
        this.dos.writeUTF(msg);
    }

    private void writeObject(final DeckInfo deck) throws IOException {

        if (deck == null) {
            throw new RuntimeException("Cannot write null Object to ObjectOutputStream.");
        }

        System.out.println(deck);
        this.objectOut.writeObject(deck);
    }
}
