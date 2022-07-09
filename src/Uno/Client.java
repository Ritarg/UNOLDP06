package Uno;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    static ObjectInputStream inObj;
    private GameControls instance;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {

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
        inObj = new ObjectInputStream(socket.getInputStream());

        // Thread que serve para o cliente enviar mensagens para o servidor
        Thread enviarMensagem = new Thread(() -> {
            instance = new GameControls(out);
            FXMLController.handleEvents(instance);
        });

        Thread lerMensagem = new Thread(() -> {
            while (true) {
                try {
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
                        // #nome-nomeJogador ou #nome-nomeJogador-pronto-vez
                        String[] msgSplit = msg.split("-");
                        // verifica se o jogo se encontra pronto para começar
                        if (msgSplit.length == 4 && msgSplit[2].equals("pronto")) {
                            instance.initGame(Boolean.parseBoolean(msgSplit[3]));
                            //instance.playerDraw(Boolean.parseBoolean(msgSplit[3]));
                            // esconde label de espera e mostra o jogo
                            Platform.runLater(() -> {
                                instance.setVisibilty(true);
                                FXMLController.nameOut1Static.setVisible(true);
                                FXMLController.nameInStatic.setVisible(false);
                                FXMLController.text_unoStatic.setVisible(false);
                            });
                        }
                    } else if (msg.startsWith("#pronto")) {
                        // #pronto-nomeJogador-vez
                        String[] msgSplit = msg.split("-");
                        instance.initGame(Boolean.parseBoolean(msgSplit[2]));
                        //instance.playerDraw(Boolean.parseBoolean(msgSplit[2]));
                        // esconde label de espera e mostra o jogo
                        Platform.runLater(() -> {
                            instance.setVisibilty(true);
                            FXMLController.nameOut1Static.setVisible(true);
                            FXMLController.nameInStatic.setVisible(false);
                            FXMLController.text_unoStatic.setVisible(false);
                        });
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        lerMensagem.start();
        enviarMensagem.start();
    }
}