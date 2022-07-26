package Uno;


public class ControlUNO {
    private IndividualCardView currentCard;

    private final CardsView unoDeck;
    //Jogador Currente - Jogador que se encontra a jogar no momento - vez do jogador
    private Player player;

    //Estabelece os nomes dos Jogadores e o Baralho de cartas de cada um
    public ControlUNO() {

        this.player = new Player();
        this.unoDeck = new CardsView();
    }

    //Método get que vai retornar o jogador do momento
    public Player getPlayer() {

        return player;
    }

    //Método set que modifica o atributo do jogador atual e a carta que ele joga
    //Não retorna nada o atributo é apenas modificado
    public void setPlayer(Player p) {
        this.player = p;
    }

    public void setCurrentCard(IndividualCardView c) {
        this.currentCard = c;
    }

    //Retorna o baralho de cartas
    public CardsView cards() {
        return unoDeck;
    }

    //Código de quando o jogador atual retira uma carta do baralho
    public void draw() {
        player.draw(unoDeck.getDrawPile().pop());
    }

    //O Jogador vai receber essa carta tirada do baralho
    public IndividualCardView getCurrentCard() {
        return currentCard;
    }

    //Imprime a cor e o número da carta
    public boolean matches(IndividualCardView c) {
        System.out.println("------------------\n" + currentCard.getColor() + " + " + c.getColor() + "=" + currentCard.getColor().equals(c.getColor()));
        System.out.println(currentCard.getNumber() + " + " + c.getNumber() + "=" + currentCard.getNumber().equals(c.getNumber()));
        System.out.println(c.getName());
        return currentCard.getNumber().equals(c.getNumber()) || currentCard.getColor().equals(c.getColor()) || currentCard.getName().equals(c.getName());
    }
}
