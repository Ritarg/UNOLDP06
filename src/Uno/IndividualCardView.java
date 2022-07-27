package Uno;

import javafx.scene.image.Image;

import java.io.Serializable;

//Classe que contém a cor, o número e a ação de cada carta;
public class IndividualCardView implements Serializable {
    //Variável que corresponde ao número da carta
    private String number;
    //Variável que corresponde à ação da carta que decorre
    private String action;
    public String color;
    //Variável que corresponde à Imagem da parte da frente da carta
    transient public Image icon;
    //Variável que corresponde à Imagem da parte de trás da carta
    transient public Image backIcon;
    //Variável que se a carta esta virada pra baixo
    public boolean isDown = false;
    //Variável que representa o nome de cada carta do UNO, que se encontram na pasta UnoCards
    private String name;

    //Estabelece a cor, o número e a ação para ir buscar a imagem da carte correspondente
    public IndividualCardView(String number, String action, String color) {
        this.color = color;
        this.number = number;
        this.action = action;

        this.name = this.color + this.action + this.number;

        setImage();
    }

    //Método que contém o caminho onde se encontram as imagens das cartas
    public void setImage() {
        //vai buscar a imagem da carta conforme o nome da mesma
        String path = "/UnoCards/" + name + ".png";
        String backPath = "/UnoCards/backSide.png";
        //Cria uma Imagem a partir do ficheiro especificado
        this.icon = new Image(path, 150, 150, true, true);
        this.backIcon = new Image(backPath, 150, 150, true, true);
    }

    public void setFaceDown(boolean s) {
        this.isDown = s;
    }

    //vai buscar o nome da carta
    public String getName() {
        return name;
    }

    //vai buscar o icon da carta
    public Image getIcon() {
        return icon;
    }

    //vai buscar o icon da parte de tras da carta
    public Image getBackIcon() {
        return backIcon;
    }

    //vai buscar o numero da carta
    public String getNumber() {
        return number;
    }

    //vai buscar a cor da carta
    public String getColor() {
        return this.color;
    }

    //estabelece a cor
    public void setColor(String color) {
        this.color = color;
    }

    //retorna o carta de nome correspondente
    public String toString() {
        return this.name;
    }
}

