package br.com.senac.bean;

import br.com.senac.view.SrvView;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thiago Emmerich
 */
public class Server implements Runnable {

    private List<ClientConnection> clientConnectionList;
    private ServerSocket serverSkt;
    private Socket socket;
    private ClientConnection connectionCliente;
    private Thread connectionThread;
    private Integer port;
    private SrvView serverView;
    private int id = 0;
    private int listSize = 0;
    private boolean power = false;

    public Server(SrvView sv) {
        this.serverView = sv;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public void run() {

        this.clientConnectionList = new ArrayList<>(); //Inicia a lista de clientes;

        try { //Tenta iniciar os servidores de mensagens e o servidor de clientes online;
            this.serverSkt = new ServerSocket(this.port); //Iniciando servidor na porta escolhida pelo usuario;
            this.serverView.setText("Iniciando servidor...");
            this.serverView.setText("Port: " + this.port);
        } catch (IOException ex) {
            this.serverView.setText("Erro ao iniciar ServerSocket");
        }

        while (true) { // Enquanto power for 'true' vai ficar esperando uma nova conexão;

            if (this.power) { // Caso power for 'false' vai parar o while dando fim ao loop;
                this.serverView.setText("Closing server...");
                break;
            }

            this.serverView.setText("Aguardando conexão...");

            try {
                this.socket = serverSkt.accept(); // Aceita o socket do cliente no servidor de mensagens;
            } catch (IOException ex) {
                this.serverView.setText("Erro ao connectar com o Cliente");
            }

            conectarClient(); // Chama o metodo
            updateListSize(); // Chama o metodo que vai atualizar o tamanho da lista de clientes;

        }
    }

    public void enviarMsg(String msg) throws IOException {
        /*
         * Percorre a lista de clientes conectados pegando o 'outPutStream' de 
         * cada para enviar a mensagem recebida no servidor pelos clientes;
         */
        String[] filtered = msg.split("//");

        if (filtered[0].equalsIgnoreCase("listaonline") || filtered[0].equalsIgnoreCase("exitmessage")) {
            for (int i = 0; i < this.listSize; i++) {
                PrintWriter pw = new PrintWriter(this.clientConnectionList.get(i).getSocket().getOutputStream(), true);
                pw.println(msg);
            }
        } else {
            if (filtered[0].equalsIgnoreCase("0")) {
                for (int i = 0; i < this.listSize; i++) {
                    PrintWriter pw = new PrintWriter(this.clientConnectionList.get(i).getSocket().getOutputStream(), true);
                    pw.println(filtered[1]);
                }
            } else {
                int identifier = Integer.parseInt(filtered[0]) - 1;
                PrintWriter pw = new PrintWriter(this.clientConnectionList.get(identifier).getSocket().getOutputStream(), true);
                pw.println("pvt//[PRIVATE] " + filtered[1]);
            }
        }
    }

    public void conectarClient() {
        /*
         * Metodo que cria uma instancia de 'ClientConnection' passando os
         * parametros, salva essa instancia na lista de conexões, inicia a 
         * Thread 'connectionThread' com esse cliente após isso inicia a Thread; 
         */
        this.connectionCliente = new ClientConnection(this.socket, this, this.id, this.serverView);
        this.clientConnectionList.add(connectionCliente);
        this.connectionThread = new Thread(this.connectionCliente);
        this.connectionThread.start();
        this.id++;
        this.serverView.setText("Conectado... ");
    }

    public void mostrarOnline() throws IOException {
        /* 
         * Vai percorrer a lista e pegar todos os ID e seus respectivos nomes
         * e enviar aos clientes;
         */
        String clientOnline = "listaonline//ID: 0 - General//";
        for (int i = 0; i < this.clientConnectionList.size(); i++) {
            clientOnline += "ID: " + (this.clientConnectionList.get(i).getId() + 1) + " - "
                    + "" + this.clientConnectionList.get(i).getNome() + "//";
        }
        enviarMsg(clientOnline);
    }

    public void shutdownServer() throws IOException {

        for (int i = 0; i < this.listSize; i++) {
            this.clientConnectionList.get(i).getSocket().close();
            this.clientConnectionList.remove(i);
            updateListSize();
        }
        this.clientConnectionList.clear();
        this.power = true;
        this.serverSkt.close();
        this.socket.close();
    }

    public void removeConnectionClosed(int id) {
        /*
         * Envia uma mensagem para os clientes informando quem saiu do chat e
         * exclui o usuario que saiu da lista de clientes conectados;
         */
        String mensagemSaida = "exitmessage//";
        mensagemSaida += this.clientConnectionList.get(id).getNome() + " saiu da conversa.";
        this.clientConnectionList.remove(id);
        updateListSize();
        try {
            enviarMsg(mensagemSaida);
        } catch (IOException ex) {
            this.serverView.setText("Failed to send exit message.");
        }
    }

    public void updateListSize() {
        this.listSize = this.clientConnectionList.size();
    }
}
