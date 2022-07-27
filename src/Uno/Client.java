package Uno;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

/**
 * Classe principal do Jogo que contém o método main() que inicializa a aplicação.
 * Ao inicializar a aplicação chama o ficheiro "playingField.fxml" que representa a interface gráfica do Jogo.
 * GameControls corresponde à classe de controlo.
 * Ficheiro "playingFields.fxml" desenvolvido no SceneBuilder.
 */

public class Client extends Application {

    private static String serverIP = "127.0.0.1";
    private static final int serverPort = 18080;
    static DataInputStream in;
    static DataOutputStream out;
    static ObjectOutputStream objOut;
    static ObjectInputStream objIn;
    private GameControls instance;
    private Stage stage;
    private String opponentName;

    @Override
    public void start(Stage primaryStage) throws IOException {

        Platform.setImplicitExit(false);
        this.stage = primaryStage;
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("playingField.fxml")));
        Scene scene = new Scene(root);

        this.stage.setScene(scene);
        this.stage.setTitle("UNO");
        this.stage.sizeToScene();
        this.stage.show();
        connectClient();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void connectClient() throws IOException {

        Socket socket = new Socket(serverIP, serverPort);

        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        objOut = new ObjectOutputStream(socket.getOutputStream());
        objIn = new ObjectInputStream(socket.getInputStream());

        // Thread que serve para o cliente enviar mensagens para o servidor
        Thread enviarMensagem = new Thread(() -> {
            Platform.setImplicitExit(false);
            instance = new GameControls(out, objOut);
            FXMLController.handleEvents(instance);
        });

        Thread lerMensagem = new Thread(() -> {
            while (true) {
                try {
                    Platform.setImplicitExit(false);
                    String msg = in.readUTF();
                    System.out.println(msg);

                    if (msg.startsWith("#salacheia")) {
                        System.out.println("Sala cheia, aguarde o fim do jogo atual...");
                        Platform.runLater(() -> {
                            System.out.println("A fechar jogo...");
                            this.stage.close();
                            System.exit(0);
                        });

                    } else if (msg.startsWith("#nome")) {
                        // msg: #nome-nomeJogador ou #nome-nomeJogador-pronto-vez
                        String[] msgSplit = msg.split("-");
                        opponentName = msgSplit[1];
                        DeckInfo opponentDeck = (DeckInfo) objIn.readObject();
                        instance.setOpponentDeck(opponentDeck);
                        // verifica se o jogo se encontra pronto para começar
                        if (msgSplit.length == 4 && msgSplit[2].equals("pronto")) {
                            instance.initGame(Boolean.parseBoolean(msgSplit[3]), false);
                        }
                        Platform.runLater(() -> {
                            // esconde label de espera e mostra o jogo
                            instance.setVisibilty(true);
                            FXMLController.nameOut1Static.setVisible(true);
                            FXMLController.nameOut2Static.setText("Jogador: " + opponentName);
                            FXMLController.nameOut2Static.setVisible(true);
                            FXMLController.nameInStatic.setVisible(false);
                            FXMLController.text_unoStatic.setVisible(false);
                        });

                    } else if (msg.startsWith("#pronto")) {
                        // msg: #pronto-nomeJogador-vez
                        String[] msgSplit = msg.split("-");
                        opponentName = msgSplit[1];
                        //objIn.defaultReadObject();
                        DeckInfo opponentDeck = (DeckInfo) objIn.readObject();
                        instance.setOpponentDeck(opponentDeck);
                        instance.setDrawPile(opponentDeck.getDrawPile());
                        instance.initGame(Boolean.parseBoolean(msgSplit[2]), true);

                        Platform.runLater(() -> {
                            // esconde label de espera e mostra o jogo
                            instance.setVisibilty(true);
                            FXMLController.nameOut1Static.setVisible(true);
                            FXMLController.nameOut2Static.setText("Jogador: " + opponentName);
                            FXMLController.nameOut2Static.setVisible(true);
                            FXMLController.nameInStatic.setVisible(false);
                            FXMLController.text_unoStatic.setVisible(false);
                        });

                    } else if (msg.startsWith("#card-drawn")) {
                        // msg: #card-drawn-true
                        String[] msgSplit = msg.split("-");
                        if (msgSplit.length == 3 && Boolean.parseBoolean(msgSplit[2])) {
                            instance.getDiscardPile().add(instance.getDrawPile().pop());
                        } else {
                            // msg: #card-drawn
                            // update drwPile e nr de cartas no campo adversario
                            Platform.runLater(() -> {
                                FXMLController.playerTwoHBoxStatic.getChildren().add(new ImageView(instance.getDrawPile().pop().getBackIcon()));
                                instance.changeTurns(true);
                            });
                        }
                        instance.logDecks();

                        Platform.runLater(() -> {
                            // esconde label de espera e mostra o jogo
                            instance.setVisibilty(true);
                            FXMLController.nameOut1Static.setVisible(true);
                            FXMLController.nameOut2Static.setVisible(true);
                            FXMLController.nameInStatic.setVisible(false);
                            FXMLController.text_unoStatic.setVisible(false);
                        });

                    } else if (msg.startsWith("#discard")) {
                        String[] msgSplit = msg.split("-");
                        // msg: #discard-wild-Number ou #discard-wild-joker
                        if (msgSplit.length >= 3) {
                            if (msgSplit[2].equals("joker")) {

                            } else {
                                for (int i = 0; i < Integer.parseInt(msgSplit[2]); i++) {
                                    instance.drawCard(true);
                                    instance.changeTurns(true);
                                }
                            }

                        } else {
                            // msg: #discard
                            Platform.runLater(() -> {
                                FXMLController.playerTwoHBoxStatic.getChildren().add(new ImageView(instance.getDrawPile().pop().getBackIcon()));
                                instance.changeTurns(true);
                            });
                        }
                        instance.logDecks();

                        Platform.runLater(() -> {
                            // esconde label de espera e mostra o jogo
                            instance.setVisibilty(true);
                            FXMLController.nameOut1Static.setVisible(true);
                            FXMLController.nameOut2Static.setVisible(true);
                            FXMLController.nameInStatic.setVisible(false);
                            FXMLController.text_unoStatic.setVisible(false);
                        });
                    }

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        enviarMensagem.start();
        lerMensagem.start();
    }
}