package Uno;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Classe auxiliar à classe {@link FXMLController}.
 * <p> Contem os principais metodos despoletados pelos eventos.
 */
public class GameControls {
    private int index;
    private ControlUNO control;
    //Variavel que corresponde as cartas do tipo Joker
    private boolean isWild;
    private boolean isGameWon = false;
    DataOutputStream out;

    /**
     * Default constructor
     */
    public GameControls() {
    }

    /**
     * Constructor
     */
    public GameControls(final DataOutputStream out) {

        //Platform.runLater(() -> {
        this.out = out;
        this.control = new ControlUNO(FXMLController.nameInStatic.getText());
        //});

    }

    //Botão inicial, Imagem/Icon do UNO que ao carregar inicia o Jogo
    public void handleStartButton() throws IOException {

        // Mostra/esconde elementos
        Platform.runLater(() -> {
            FXMLController.nameOut1Static.setText(FXMLController.nameInStatic.getText());
            FXMLController.nameOut1Static.setVisible(false);
            FXMLController.nameInStatic.setVisible(false);
            FXMLController.text_unoStatic.setVisible(false);
            //});
            setVisibilty(false);

            for (int i = 0; i < control.cards().getDrawPile().size(); i++) {
                ImageView temp = new ImageView(control.cards().getDrawPile().get(i).getBackIcon());
                Platform.runLater(() -> FXMLController.drawPileStatic.getChildren().add(temp));
            }
            //As cartas são baralhadas para dar aos jogadores
            reshuffle();

            //Platform.runLater(() -> {
            String msg = "#nome-" + FXMLController.nameInStatic.getText();
            System.out.println("Mensagem enviada pelo Jogador " + control.getPlayer().getName() + ": " + msg);
            try {
                this.out.writeUTF(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //});

            //initGame(false);
            //Cartas do Jogador 2 que aparecem escondidas, ou seja mostra apenas a parte de tras das cartas
            IndividualCardView top = control.cards().getDrawPile().peek();
            top.setFaceDown(true);
            //Vai buscar as cartas ao baralho
            IndividualCardView tempCard = control.cards().getDrawPile().pop();
            ImageView temp = new ImageView(tempCard.getIcon());
            control.cards().getDiscardPile().add(tempCard);
            //Platform.runLater(() -> {
            FXMLController.drawPileStatic.getChildren().remove(FXMLController.drawPileStatic.getChildren().size() - 1);
            FXMLController.discardPileStatic.getChildren().add(temp);
            //Remove a imagem do Logo
            FXMLController.anchorStatic.getChildren().remove(FXMLController.startStatic);
            //});
            //Mostra a carta inicial jogada
            control.setCurrentCard(control.cards().getDiscardPile().peek());
        });
    }

    /* Método Draw - Jogadores fazem uma jogada
     * txtField - Campo de texto com comentação dos passos de cada jogador
     * As cartas recebidas vêm da pilha de cartas do StackPane com o fx:id="drawPile"
     * As cartas descartadas vão para a pilha de cartas do StackPane com o fx:id="discardPile"
     * playerOneHBox - container onde se encontram exibidas as cartas do Jogador 1
     * playerTwoHBox - container onde se encontram exibidas as cartas do Jogador 2*/
    public void drawCards(final boolean isInitialPlay) throws IOException {

        Platform.runLater(() -> {
            FXMLController.drawPileStatic.getChildren().remove(FXMLController.drawPileStatic.getChildren().size() - 1);
            control.getPlayer().draw(control.cards().getDrawPile().pop());
            ImageView temp = new ImageView(control.cards().getDrawPile().peek().getIcon());
            FXMLController.playerOneHBoxStatic.getChildren().add(temp);
            FXMLController.txtFieldStatic.setText("\nJogaste uma carta, é a vez do adversario");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            //});

            // se nao for initGame, envia msg ao outro jogador
            if (!isInitialPlay) {
                String msg = "#card-draw-" + control.getPlayer();
                System.out.println("Mensagem enviada pelo Jogador " + control.getPlayer().getName() + ": " + msg);
                try {
                    this.out.writeUTF(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                control.getPlayer().setMyTurn(false);
            }
        });
    }

    //Passo inicial do Jogo onde os Jogadores recebem uma mão de 7 Cartas
    public void initGame(final boolean myTurn) throws IOException {

        Platform.runLater(() -> {
            for (int i = 0; i < 7; i++) {
                try {
                    drawCards(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //Platform.runLater(() -> FXMLController.txtFieldStatic.setText("\nTodos os Jogadores recebem 7 cartas"));
                FXMLController.txtFieldStatic.setText("\nTodos os Jogadores recebem 7 cartas");
            }

            control.getPlayer().setMyTurn(myTurn);
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
                //});
                //Se o Jogador 1 jogar uma carta Joker+4 este deve selecionar a cor que deseja
                //O Jogador 2 recebe 4 cartas aleatoriamente tiradas do baralho
                String playerCard = control.getPlayer().getCards().get(index).getName();
                if (control.getPlayer().getCards().get(index).getName().equals("noneplusfour-1")) {
                    playerOneDiscard(index);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    for (int i = 0; i < 4; i++) {
                        //playerTwoDraw();
                    }
                    //Platform.runLater(() -> {
                    FXMLController.txtFieldStatic.setText("\nJogaste uma carta Joker+4, escolhe a cor");
                    FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
                    if (control.getPlayer().getCards().size() == 0) {
                        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");

                    }
                    //});

                    isWild = true;
                    isVisible(true);
                    //control.setCurrentPlayer(control.getPlayerTwo());

                    //Se o Jogador 1 jogar uma carta Joker este deve selecionar a cor que deseja
                } else if (control.getPlayer().getCards().get(index).getName().equals("nonewild-1")) {
                    //Platform.runLater(() -> {
                    FXMLController.txtFieldStatic.setText("\nJogaste uma carta Joker, escolhe a cor");
                    FXMLController.txtFieldStatic.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                    //});

                    playerOneDiscard(index);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    if (control.getPlayer().getCards().size() == 0) {
                        //Platform.runLater(() -> FXMLController.txtFieldStatic.setText("Ganhaste o Jogo"));
                        FXMLController.txtFieldStatic.setText("Ganhaste o Jogo");
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
                    control.cards().getDiscardPile().push(control.getPlayer().getCards().get(index));
                    playerOneDiscard(index);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    for (int i = 0; i < 2; i++) {
                        //playerTwoDraw();
                    }
                    /*Se o tamanho for igual a 0, significa que o Jogador não possui mais cartas
                     * ou seja, este ganha o jogo */
                    if (control.getPlayer().getCards().size() == 0) {
                        //Platform.runLater(() -> {
                        FXMLController.txtFieldStatic.setText("Ganhaste o Jogo");
                        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
                        //});

                        setGameWon(true);
                    }
                    //control.setCurrentPlayer(control.getPlayerTwo());
                } else if (control.matches(control.getPlayer().getCards().get(index))) {
                    control.cards().getDiscardPile().push(control.getPlayer().getCards().get(index));
                    playerOneDiscard(index);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    if (control.getPlayer().getCards().size() == 0) {
                        //Platform.runLater(() -> {
                        FXMLController.txtFieldStatic.setText("Ganhaste o Jogo");
                        FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
                        //});

                        setGameWon(true);
                    }
                    //control.setCurrentPlayer(control.getPlayerTwo());
                }
            });
        }
    }

    /* Manipula as ações do Jogador2 */
    /*public void playerTwoHandles() {
        if (isGameWon == false) {
            if (control.getCurrentPlayer().getCards().size() == 0) {
                txtField.setText("Jogador 2 ganhou");
                txtField.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

            }
            if (control.cards().getDrawPile().size() == 0) {
                reshuffle();
            }
            String[] color = {"yellow", "blue", "red", "green"};
            String playerTwoChoice = "";
            for (int i = 0; i < control.getPlayerTwo().getCards().size(); i++) {
                IndividualCardView playerCard = control.getPlayerTwo().getCards().get(i);
                if (control.matches((control.getPlayerTwo().getCards().get(i))) == false) {
                    continue;
                }
                if (playerCard.getName().substring(playerCard.getName().length() - 9, playerCard.getName().length() - 2).equals("plustwo") &&
                        control.matches(control.getPlayerTwo().getCards().get(i))) {

                } else if (control.matches(control.getPlayerTwo().getCards().get(i)) == true && !(playerCard.getNumber().equals("-1"))) {
                    //playerTwoDiscard(i);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    if (control.getCurrentPlayer().getCards().size() == 0) {
                        txtField.setText("Jogador 2 ganhou");
                        txtField.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                        setGameWon(true);
                    }
                    control.setCurrentPlayer(control.getPlayerOne());
                    return;
                } else if (control.getPlayerTwo().getCards().get(i).getName().equals("noneplusfour-1")) {
                    playerTwoChoice = color[(int) (Math.random() * (color.length))];


                    //playerTwoDiscard(i);

                    control.cards().getDiscardPile().peek().setColor(playerTwoChoice);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    if (control.getCurrentPlayer().getCards().size() == 0) {
                        txtField.setText("Jogador 2 ganhou");
                        txtField.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                        setGameWon(true);

                    }
                    for (int j = 0; j < 4; j++) {
                        playerOneDraw();
                    }
                    txtField.setText("Jogador 2 jogou carta Joker+4, escolheu a cor " + playerTwoChoice);
                    txtField.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");

                    control.setCurrentPlayer(control.getPlayerOne());
                    return;
                } else if (control.getPlayerTwo().getCards().get(i).getName().equals("nonewild-1")) {
                    playerTwoChoice = color[(int) (Math.random() * (color.length))];
                    txtField.setText("Jogador 2 jogou carta Joker, escolheu a cor  " + playerTwoChoice);
                    txtField.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");


                    //playerTwoDiscard(i);

                    control.cards().getDiscardPile().peek().setColor(playerTwoChoice);
                    control.setCurrentCard(control.cards().getDiscardPile().peek());
                    if (control.getCurrentPlayer().getCards().size() == 0) {
                        txtField.setText("Jogador 2 ganhou");
                        txtField.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                        setGameWon(true);

                    }
                    control.setCurrentPlayer(control.getPlayerOne());
                    return;
                }


            }
            //playerTwoDraw();
        }
    }*/

    /* Baralha as cartas */
    public void reshuffle() {

        control.cards().shuffleDrawPile();

        Platform.runLater(() -> {
            if (FXMLController.discardPileStatic.getChildren().size() > 0) {
                for (int i = 0; i < FXMLController.discardPileStatic.getChildren().size() - 1; i++) {
                    FXMLController.discardPileStatic.getChildren().remove(i);
                }
            }

            for (int i = 0; i < control.cards().getDrawPile().size(); i++) {
                ImageView temp = new ImageView(control.cards().getDrawPile().get(i).getBackIcon());
                FXMLController.drawPileStatic.getChildren().add(i, temp);
            }

            FXMLController.txtFieldStatic.setText("Shuffled the draw deck");
            FXMLController.txtFieldStatic.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        });
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

    //Descarte do Jogador 1
    public void playerOneDiscard(int n) {

        control.cards().getDiscardPile().push(control.getPlayer().remove(control.getPlayer().getCards().get(n)));
        Platform.runLater(() -> FXMLController.discardPileStatic.getChildren().add(FXMLController.playerOneHBoxStatic.getChildren().remove(n)));
    }

    //Descarte do Jogador 2
    /*public void playerTwoDiscard(int n) {
        if (control.matches(control.getPlayerTwo().getCards().get(n))) {
            ImageView temp = new ImageView(control.getPlayerTwo().getCards().get(n).getIcon());
            control.cards().getDiscardPile().push(control.getPlayerTwo().getCards().remove(n));
            playerTwoHBox.getChildren().remove(n);
            discardPile.getChildren().add(temp);
        }
    }*/

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

        //Platform.runLater(() -> {
        FXMLController.txtFieldStatic.setVisible(visible);
        FXMLController.drawPileStatic.setVisible(visible);
        FXMLController.discardPileStatic.setVisible(visible);
        FXMLController.playerOneHBoxStatic.setVisible(visible);
        FXMLController.playerTwoHBoxStatic.setVisible(visible);
        FXMLController.drawButtonStatic.setVisible(visible);
        FXMLController.drawButtonStatic.setVisible(visible);
        FXMLController.text_waitStatic.setVisible(!visible);
        //});
    }

    //Quando uma carta Joker é jogada torna se visivel a seleção da cor para a carta da proxima jogada
    public void isVisible(boolean visible) {

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
}
