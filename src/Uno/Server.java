package Uno;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server {
    private static final int serverPort = 18080;
    private static Socket s;
    // lista para guardar clientes
    private static final List<ClientHandler> listaClientes = new ArrayList<>();
    // lista para guardar os nomes dos clientes
    private static final List<String> nomesClientes = new ArrayList<>();
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
                        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                        ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());

                        ClientHandler mtch = new ClientHandler(s, "client " + i, dis, dos, i, in, objOut);
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

        Socket s;
        private final String code;
        final DataInputStream dis;
        final DataOutputStream dos;
        final int id;
        ObjectInputStream in;
        ObjectOutputStream objOut;
        private String name;
        boolean isloggedin;


        private ClientHandler(Socket s, String code, DataInputStream dis, DataOutputStream dos, int id, ObjectInputStream objIn, ObjectOutputStream objOut) {

            this.s = s;
            this.code = code;
            this.dis = dis;
            this.dos = dos;
            this.id = id;
            this.in = objIn;
            this.objOut = objOut;
            this.name = null;
            this.isloggedin = true;
        }

        private boolean geraVez() {
            boolean result = false;
            Random r = new Random();
            int random = r.nextInt();
            if (random % 2 == 0) result = true;
            return result;
        }

        private String getNomeOutroJogador(final String codeJogador) {
            String result = "";
            for (ClientHandler c : listaClientes) {
                if (!c.code.equals(codeJogador)) result = c.name;
            }
            return result;
        }

        static String recebido;

        @Override
        public void run() {

            if (Server.listaClientes.size() > 2) {
                try {
                    String msg = "#salacheia";
                    System.out.println("Mensagem enviada pelo Servidor: " + msg);
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
                            String nomeAux = recebido.split("-")[1];
                            System.out.println("Jogador " + nomeAux + " pronto.");
                            nomesClientes.add(nomeAux);
                            name = nomeAux;
                            // quando ele tiver 2 jogadores inicia o jogo
                            boolean ready = listaClientes.size() == 2;
                            boolean vez = this.geraVez();
                            for (ClientHandler c : listaClientes) {
                                if (!c.code.equals(code) && c.isloggedin) {
                                    // #nome-nomeJogador-pronto-vez
                                    String message = "#nome-" + nomeAux + "-" + "pronto-" + !vez;
                                    System.out.println("Mensagem enviada pelo Servidor: " + message);
                                    c.dos.writeUTF(message);
                                } else {
                                    if (ready) {
                                        // #pronto-nomeAdversario-vez
                                        String msg = "#pronto-" + getNomeOutroJogador(code) + "-" + vez;
                                        System.out.println("Mensagem enviada pelo Servidor: " + msg);
                                        c.dos.writeUTF(msg);
                                    }
                                }
                            }
                        }
                        // ao receber a mensagem ele ira enviar ao outro jogador a informacao desta jogada
                        /*if (recebido.startsWith("Jogador#")) {
                            String[] r = recebido.split("#");
                            System.out.println(r[1]);
                            for (ClientHandler c : listaClientes) {
                                if (!c.code.equals(code)) {
                                    c.dos.writeUTF(recebido);
                                }
                            }

                        }*/

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            client.start();
        }
    }
}
