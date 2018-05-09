package br.com.senac.bean;

import br.com.senac.view.SrvView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Thiago Emmerich
 */
public class ClientConnection implements Runnable {

    private Socket socket;
    private Server server;
    private String nome;
    private InputStream inputStream;
    private InputStreamReader inRead;
    private BufferedReader bRead;
    private PrintWriter printWriter;
    private String linha;
    private int id;
    private SrvView serverView;

    public ClientConnection(Socket skt, Server srv, int id, SrvView sv) {
        this.socket = skt;
        this.server = srv;
        this.id = id;
        this.serverView = sv;
    }

    @Override
    public void run() {

        try {

            iniciar(); //Inicia todas as variaveis
            setName(); //Chama o metodo que nomeia cada cliente

            while (true) {

                this.printWriter.println("Digite um texto:");

                while (true) {
                    this.server.mostrarOnline();
                    this.linha = this.bRead.readLine();
                    enviarMsg(this.linha);
                }
            }
        } catch (IOException ex) {
            this.serverView.setText("Client error...");
            this.server.removeConnectionClosed(this.id);
            try {
                this.socket.close();
                this.serverView.setText("Connection closed!");
            } catch (IOException ex1) {
                this.serverView.setText("Failed to close connection");
            }
        }
    }

    public void iniciar() throws IOException {
        this.inputStream = this.socket.getInputStream();
        this.inRead = new InputStreamReader(inputStream);
        this.bRead = new BufferedReader(inRead);
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    public void setName() throws IOException {
        this.printWriter.println("qualseunome//");
        this.nome = bRead.readLine();
    }

    public void enviarMsg(String msg) throws IOException {
        this.server.enviarMsg(msg);
    }

    public int getId() {
        return this.id;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getNome() {
        return this.nome;
    }
}
