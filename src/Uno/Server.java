package Uno;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Server {
    private static final int serverPort = 18080;
    private static Socket s;
    // lista para guardar clientes
    private static final List<ClientHandler> listaClientes = new ArrayList<>();
    private static int i = 0;

    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(serverPort);
        System.out.println("Servidor aceita conexões na porta " + serverPort);

        Thread servidor = new Thread(() -> {
            // limitar a conecçao para ter no maximo dois clientes
            if (i < 2) {
                while (true) {
                    try {
                        // ele aqui aceita a coneccao de um cliente
                        s = ss.accept();
                        System.out.println("Novo cliente recebido : " + s);
                        // buscar o input e output do cliente
                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                        ObjectInputStream objIn = new ObjectInputStream(s.getInputStream());
                        ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());

                        ClientHandler mtch = new ClientHandler(s, "client " + i, dis, dos, i, objIn, objOut);
                        Thread t = new Thread(mtch);
                        // a lista de clientes serve para ter rapido acesso aos clientes na logica
                        listaClientes.add(mtch);
                        t.start();
                        i++;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        servidor.start();
    }

    /**
     * Gerir a interacao entre cliente e servidor
     */
    private static class ClientHandler implements Runnable {

        private final Socket s;
        private final String code;
        private final DataInputStream dis;
        private final DataOutputStream dos;
        private final int id;
        private final ObjectInputStream objIn;
        private final ObjectOutputStream objOut;
        private String name;
        private boolean isloggedin;
        private boolean isReady;
        private static String recebido;
        private List<IndividualCardView> deck;
        private Stack<IndividualCardView> drawPile;


        private ClientHandler(Socket s, String code, DataInputStream dis, DataOutputStream dos, int id, ObjectInputStream objIn, ObjectOutputStream objOut) {

            this.s = s;
            this.code = code;
            this.dis = dis;
            this.dos = dos;
            this.id = id;
            this.objIn = objIn;
            this.objOut = objOut;
            this.name = null;
            this.isloggedin = true;
            this.isReady = false;
        }

        private boolean randomTurn() {

            boolean result = false;

            int random = new Random().nextInt();
            if (random % 2 == 0) result = true;

            return result;
        }

        private String getOpponentName(final String codeJogador) {

            String result = "";
            for (ClientHandler c : listaClientes) {
                if (!c.code.equals(codeJogador)) result = c.name;
            }

            return result;
        }

        private boolean isReady() {

            boolean result = true;

            if (Server.listaClientes.size() < 2) {
                result = false;
            } else {
                for (ClientHandler c : Server.listaClientes) {
                    if (!c.isReady) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }

        @Override
        public void run() {

            if (Server.listaClientes.size() > 2) {
                try {
                    String msg = "#salacheia";
                    System.out.println("Mensagem enviada: " + msg);
                    dos.writeUTF(msg);
                    // remove da lista de clientes
                    Server.listaClientes.remove(this);

                    this.isloggedin = false;
                    this.s.close();

                    return;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            Thread client = new Thread(() -> {
                while (true) {
                    // receber as mensagens dos clientes e processa-las
                    try {
                        recebido = dis.readUTF();

                        if (recebido.startsWith("#nome")) {
                            // msg: #nome-nomeJogador
                            name = recebido.split("-")[1];
                            System.out.println("Jogador " + name + " pronto.");
                            DeckInfo deckInfo = (DeckInfo) objIn.readObject();
                            deck = deckInfo.getDeck();
                            drawPile = deckInfo.getDrawPile();
                            System.out.println("Deck adversario: \n" + deckInfo);
                            isReady = true;
                            // quando tiver 2 jogadores inicia o jogo
                            boolean ready = isReady();
                            boolean turn = this.randomTurn();

                            for (ClientHandler c : listaClientes) {
                                if (!c.code.equals(code) && c.isloggedin) {
                                    // enviar: #nome-nomeJogador
                                    String message = "#nome-" + name;
                                    if (ready) {
                                        // enviar: #nome-nomeJogador-pronto-vez
                                        message += "-" + "pronto-" + !turn;
                                    }
                                    System.out.println("Mensagem enviada: " + message);
                                    c.dos.writeUTF(message);
                                    c.objOut.writeObject(deckInfo);
                                } else {
                                    if (ready) {
                                        // #pronto-nomeAdversario-vez
                                        String msg = "#pronto-" + getOpponentName(code) + "-" + turn;
                                        System.out.println("Mensagem enviada: " + msg);
                                        c.dos.writeUTF(msg);
                                    }
                                }
                            }
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            client.start();
        }
    }
}
