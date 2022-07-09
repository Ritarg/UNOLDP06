/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Uno;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class de controlo para o fxml que gere todos os eventos
 */
public class FXMLController implements Initializable {

    /* Initialize FXML tags */
    @FXML
    private AnchorPane anchor;
    public static AnchorPane anchorStatic;
    @FXML
    private TextArea txtField;
    public static TextArea txtFieldStatic;
    //da cartas
    @FXML
    private StackPane drawPile;
    public static StackPane drawPileStatic;
    //recebe cartas
    @FXML
    private StackPane discardPile;
    public static StackPane discardPileStatic;
    @FXML
    private HBox playerOneHBox;
    public static HBox playerOneHBoxStatic;
    @FXML
    private Rectangle playerOne;
    public static Rectangle playerOneStatic;
    @FXML
    private HBox playerTwoHBox;
    public static HBox playerTwoHBoxStatic;
    @FXML
    private Button drawButton;
    public static Button drawButtonStatic;
    @FXML
    private Button red;
    public static Button redStatic;
    @FXML
    private Button green;
    public static Button greenStatic;
    @FXML
    private Button yellow;
    public static Button yellowStatic;
    @FXML
    private Button blue;
    public static Button blueStatic;
    @FXML
    private ImageView start;
    public static ImageView startStatic;
    @FXML
    public TextField nameIn;
    public static TextField nameInStatic;
    @FXML
    private Text nameOut1;
    public static Text nameOut1Static;
    @FXML
    private Text text_uno;
    public static Text text_unoStatic;
    @FXML
    private Text text_wait;
    public static Text text_waitStatic;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        anchorStatic = anchor;
        txtFieldStatic = txtField;
        drawPileStatic = drawPile;
        discardPileStatic = discardPile;
        playerOneHBoxStatic = playerOneHBox;
        playerOneStatic = playerOne;
        playerTwoHBoxStatic = playerTwoHBox;
        drawButtonStatic = drawButton;
        redStatic = red;
        greenStatic = green;
        yellowStatic = yellow;
        blueStatic = blue;
        startStatic = start;
        nameInStatic = nameIn;
        nameOut1Static = nameOut1;
        text_unoStatic = text_uno;
        text_waitStatic = text_wait;
        text_waitStatic.setVisible(false);
    }

    /**
     * Gestao dos eventos despoletados pelos recursos presentes em {@code playingField.fxml}
     */
    public static void handleEvents(final GameControls instance) {

        startStatic.setOnMouseClicked(event -> {

            try {
                instance.handleStartButton();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        drawButtonStatic.setOnMouseClicked(event -> {

            try {
                instance.drawCards(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        redStatic.setOnMouseClicked(event -> {

            instance.redClicked();
        });

        greenStatic.setOnMouseClicked(event -> {

            instance.greenClicked();
        });

        yellowStatic.setOnMouseClicked(event -> {

            instance.yellowClicked();
        });

        blueStatic.setOnMouseClicked(event -> {

            instance.blueClicked();
        });

        playerOneStatic.setOnMouseEntered(event -> {

            instance.select();
        });
    }
}
